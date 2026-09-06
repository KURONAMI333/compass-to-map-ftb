package com.kuronami.compasstomapftb.compat.ftb;

import com.kuronami.compasstomapftb.CompassToMapFtb;
import com.kuronami.compasstomapftb.client.CompassNames;
import com.kuronami.compasstomapftb.client.Discovery;
import com.kuronami.compasstomapftb.client.Notifier;
import com.kuronami.compasstomapftb.client.SeenKeys;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.waypoint.Waypoint;
import dev.ftb.mods.ftbchunks.api.client.waypoint.WaypointManager;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 発見（{@link Discovery}）を FTB Chunks の waypoint として登録する層。
 *
 * <p>manager が未準備（ログイン直後の MapManager 初期化前）の間は保留キューへ積み、
 * {@link #tick()}（20 tick ごとに W1 の {@code ClientTickEvent} 購読から呼ばれる）で
 * 再試行する。このクラス自身は {@code @EventBusSubscriber} を持たない。
 */
public final class FtbWaypointSink {

    /** 保留エントリが積まれてからこれを超えたら破棄する（tick 単位、20 tick = 1秒として約10秒）。 */
    private static final int MAX_PENDING_TICKS = 200;

    /** 保留キューの最大件数。溢れたら最古のものから捨てる。 */
    private static final int MAX_PENDING_SIZE = 64;

    private static final Deque<Pending> PENDING = new ArrayDeque<>();

    /**
     * 破棄の warn を出した key。**同じ発見について warn は1回だけ**にする。
     *
     * <p>破棄時に {@code SeenKeys} の記録を取り消すので、コンパスが FOUND のままなら
     * 次の走査がまた登録を試み、また保留になり、また破棄される。これは
     * 「あとで manager が取れるようになったら拾う」ための正しいリトライだが、
     * warn まで毎回出すと**サーバーに FTB Chunks が無い構成でログが延々と流れる**
     * （SPEC §7 D0 のとおり、その構成では WaypointManager は永久に空）。
     */
    private static final Set<String> WARNED_DROPS = new HashSet<>();

    private FtbWaypointSink() {}

    /** 発見1件を登録する。manager が未準備なら保留キューへ積む。 */
    public static void offer(Discovery d, int y) {
        try {
            Optional<WaypointManager> mgr = FTBChunksAPI.clientApi().getWaypointManager(d.dimension());
            if (mgr.isPresent()) {
                register(mgr.get(), d, y);
                return;
            }
        } catch (Throwable t) {
            CompassToMapFtb.LOGGER.warn("FTB Chunks waypoint manager lookup failed: {}", t.toString());
            enqueue(d, y).warned = true;
            return;
        }
        enqueue(d, y);
    }

    /** クライアント tick から 20 tick ごとに呼ばれる。保留キューを処理する。 */
    public static void tick() {
        if (PENDING.isEmpty()) return;

        int size = PENDING.size();
        for (int i = 0; i < size; i++) {
            Pending p = PENDING.pollFirst();
            if (p == null) break;

            p.ageTicks += 20;
            if (p.ageTicks > MAX_PENDING_TICKS) {
                drop(p, "timed out waiting for the FTB Chunks waypoint manager");
                continue;
            }

            try {
                Optional<WaypointManager> mgr = FTBChunksAPI.clientApi().getWaypointManager(p.discovery.dimension());
                if (mgr.isPresent()) {
                    register(mgr.get(), p.discovery, p.y);
                } else {
                    PENDING.addLast(p);
                }
            } catch (Throwable t) {
                if (!p.warned) {
                    p.warned = true;
                    CompassToMapFtb.LOGGER.warn("FTB Chunks waypoint manager lookup failed: {}", t.toString());
                }
                PENDING.addLast(p);
            }
        }
    }

    /** ログアウト時に保留を捨てる。 */
    public static void clearPending() {
        PENDING.clear();
        WARNED_DROPS.clear();
    }

    private static Pending enqueue(Discovery d, int y) {
        if (PENDING.size() >= MAX_PENDING_SIZE) {
            Pending dropped = PENDING.pollFirst();
            if (dropped != null) {
                drop(dropped, "pending queue full");
            }
        }
        Pending p = new Pending(d, y);
        PENDING.addLast(p);
        return p;
    }

    /**
     * 保留を捨てる。**捨てたら SeenKeys の記録も取り消す。**
     *
     * <p>取り消さないと、コンパスが FOUND のまま手元にあっても検出層が
     * 「もう見た key だ」と弾き続け、そのセッション中は二度と登録されない。
     * 取り消しておけば、次の走査で同じ発見がもう一度登録を試みる。
     */
    private static void drop(Pending p, String reason) {
        String key = p.discovery.key();
        SeenKeys.remove(key);
        if (!WARNED_DROPS.add(key)) return;
        CompassToMapFtb.LOGGER.warn("Dropped waypoint for {} at ({}, {}) in {}: {}",
                p.discovery.id(), p.discovery.x(), p.discovery.z(),
                p.discovery.dimension().location(), reason);
    }

    private static void register(WaypointManager mgr, Discovery d, int y) {
        try {
            String name = CompassNames.prettify(d.id());

            // 既存の照合も種別で分ける（Discovery#key() と同じ理由）。
            // BIOME は座標がぶれるので「この次元に同じ名前のピンが既にあるか」で見る。
            // STRUCTURE は座標が決定的なので x/z で見る（別の村には別のピンが立つ）。
            // Y はどちらでも見ない（チャンクのロード状況で変わるため）。
            boolean exists = switch (d.kind()) {
                case STRUCTURE -> mgr.getAllWaypoints().stream()
                        .anyMatch(w -> w.getPos().getX() == d.x() && w.getPos().getZ() == d.z());
                case BIOME -> mgr.getAllWaypoints().stream()
                        .anyMatch(w -> name.equals(w.getName()));
            };
            if (exists) return;
            // addWaypointAt は「追加できたか」を返さない。内部で HashSet#add に渡した後、
            // 挿入の成否に関わらず新しく作った WaypointImpl をそのまま返す
            // (WaypointManagerImpl:155-159)。だから非 null は登録された証拠にならず、
            // 重複の判定は上の x/z 照合だけが担っている。
            Waypoint wp = mgr.addWaypointAt(new BlockPos(d.x(), y, d.z()), name);
            if (wp == null) return;
            wp.setColor(WaypointColors.forDiscovery(d));
            Notifier.added(name, d.x(), d.z());
        } catch (Throwable t) {
            CompassToMapFtb.LOGGER.warn("FTB Chunks waypoint registration failed: {}", t.toString());
        }
    }

    private static final class Pending {
        final Discovery discovery;
        final int y;
        int ageTicks;
        /** この保留について既に warn を出したか（同じ失敗を毎リトライ出さない）。 */
        boolean warned;

        Pending(Discovery discovery, int y) {
            this.discovery = discovery;
            this.y = y;
        }
    }
}
