package com.kuronami.compasstomapftb.compat.ftb;

import com.kuronami.compasstomapftb.CompassToMapFtb;
import com.kuronami.compasstomapftb.client.CompassNames;
import com.kuronami.compasstomapftb.client.Discovery;
import com.kuronami.compasstomapftb.client.Notifier;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.waypoint.Waypoint;
import dev.ftb.mods.ftbchunks.api.client.waypoint.WaypointManager;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

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
                CompassToMapFtb.LOGGER.warn(
                        "FTB Chunks waypoint pending timed out, dropping: {}", p.discovery.id());
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
                CompassToMapFtb.LOGGER.warn("FTB Chunks waypoint manager lookup failed: {}", t.toString());
                PENDING.addLast(p);
            }
        }
    }

    /** ログアウト時に保留を捨てる。 */
    public static void clearPending() {
        PENDING.clear();
    }

    private static void enqueue(Discovery d, int y) {
        if (PENDING.size() >= MAX_PENDING_SIZE) {
            Pending dropped = PENDING.pollFirst();
            if (dropped != null) {
                CompassToMapFtb.LOGGER.warn(
                        "FTB Chunks waypoint pending queue full, dropping oldest: {}", dropped.discovery.id());
            }
        }
        PENDING.addLast(new Pending(d, y));
    }

    private static void register(WaypointManager mgr, Discovery d, int y) {
        try {
            boolean exists = mgr.getAllWaypoints().stream()
                    .anyMatch(w -> w.getPos().getX() == d.x() && w.getPos().getZ() == d.z());
            if (exists) return;

            String name = CompassNames.prettify(d.id());
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

        Pending(Discovery discovery, int y) {
            this.discovery = discovery;
            this.y = y;
        }
    }
}
