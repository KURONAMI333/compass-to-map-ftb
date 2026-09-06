package com.kuronami.compasstomapftb.client;

import com.kuronami.compasstomapftb.CompassToMapFtb;
import com.kuronami.compasstomapftb.Config;
import com.kuronami.compasstomapftb.compat.ftb.FtbWaypointSink;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * コンパスの検出層。クライアント tick からインベントリ（防具・オフハンド込み）を走査し、
 * Nature's Compass / Explorer's Compass が FOUND を返していれば {@link Discovery} を作って
 * 登録層（{@link FtbWaypointSink}）へ渡す。
 *
 * <p>EC / NC はどちらも optional 依存。API 不一致・クラス不在で本体クラスのロード自体が
 * 巻き込まれないよう、各 MOD への参照は {@link ECInner} / {@link NCInner} に閉じ込める
 * （C2M（mod-003）の {@code CompassWatcher} の ECInner / NCInner と同じ形。この2クラスの
 * 実データが実際に読めることは、同じ手法の {@code G1Spike}／{@code ECProbe}／{@code NCProbe}
 * で先に確認済み）。
 */
@EventBusSubscriber(modid = CompassToMapFtb.MODID, value = Dist.CLIENT)
public final class CompassScanner {

    /** インベントリ走査の間隔（tick）。 */
    private static final int SCAN_INTERVAL_TICKS = 10;

    /** ログイン直後に「記録だけして登録しない」走査を何回行うか（10 tick 間隔なので 3 回 = 30 tick）。 */
    private static final int PRIMING_SCANS = 3;

    /** {@link FtbWaypointSink#tick()} を呼ぶ間隔（tick）。 */
    private static final int SINK_TICK_INTERVAL_TICKS = 20;

    private static int tick = 0;

    /**
     * ログイン直後、既に FOUND のコンパスを「発見済み」として記録するだけで登録しない走査の残り回数。
     *
     * <p>コンパスは検索結果を持ち越すので、ログイン時点で FOUND なのは<b>前のセッションの結果</b>で
     * あって新しい発見ではない。そのまま登録すると、別の次元でログインした時にそこへ他次元の座標が立つ。
     * インベントリの同期はログイン直後に届くが1 tick 精度で保証されないので、数回ぶんの余裕を取る。
     * この窓（30 tick ＝ 1.5 秒）の間に実際の検索が完了することはない（GUI を開いて対象を選ぶ操作が要る）。
     */
    private static int primingScans = 0;

    /**
     * EC / NC が導入されているか。**未導入を例外で検出しない**ための門番。
     *
     * <p>Inner class の隔離だけでも落ちはしないが、それだと「EC を入れていないだけ」の
     * 正規の構成（SPEC F3）でも NoClassDefFoundError の warn が log に出てしまう。
     * C2M（mod-003）の {@code CompassWatcher:74,86} と同じく先に {@link ModList} で弾く。
     * Inner class の catch は残す（導入されているが API が変わった場合の受け皿）。
     *
     * <p><b>static final で持たない。</b> {@code @EventBusSubscriber} のクラスがいつロードされるかは
     * 同居する MOD の顔ぶれで変わり、{@link ModList} が揃う前にロードされると false のまま固まる。
     * 実際に JourneyMap と C2M を同居させた構成で検出が丸ごと止まった（2026-09-06 実測・受入 A9）。
     * 最初の走査（＝ワールドに入った後）で1回だけ解決する。
     */
    private static Boolean ecLoaded;
    private static Boolean ncLoaded;

    private static boolean compassModsResolved() {
        if (ecLoaded != null) return true;
        try {
            ecLoaded = ModList.get().isLoaded("explorerscompass");
            ncLoaded = ModList.get().isLoaded("naturescompass");
        } catch (Throwable t) {
            return false; // ModList がまだ使えない。次の走査で試す
        }
        CompassToMapFtb.LOGGER.info("Compass detection ready: Explorer's Compass={} / Nature's Compass={}",
                ecLoaded, ncLoaded);
        return true;
    }

    private CompassScanner() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tick++;

        Minecraft mc = Minecraft.getInstance();
        // タイトル画面では保留キューも回さない（ワールドに居ないので manager は取れず、
        // 空振りのリトライで保留が寿命を削られるだけになる）。
        if (mc.player == null) return;

        // 保留キューの再試行（ログイン直後の MapManager 初期化待ちを拾う）。
        if (tick % SINK_TICK_INTERVAL_TICKS == 0) {
            FtbWaypointSink.tick();
        }

        if (tick % SCAN_INTERVAL_TICKS != 0) return;

        Level level = mc.player.level();
        ResourceKey<Level> dimension = level.dimension();

        // getContainerSize() は items(36) + armor(4) + offhand(1) = 41 を返す
        // （1.21.1 の net.minecraft.world.entity.player.Inventory を javap で実測確認済み）。
        // C2M（mod-003）のように offhand だけ別扱いで読む必要はなく、この1ループで
        // メインインベントリ・防具・オフハンドの全スロットを漏れなく走査できる。
        if (!compassModsResolved()) return;

        boolean priming = primingScans > 0;
        if (priming) primingScans--;

        Inventory inv = mc.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            // C2M は最初の1本で打ち切るが、本作は2本同時 FOUND を取りこぼさないため
            // break せずに全スロットを見続ける。
            if (ecLoaded) ECInner.tryHandle(stack, level, dimension, priming);
            if (ncLoaded) NCInner.tryHandle(stack, level, dimension, priming);
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        SeenKeys.clear();
        FtbWaypointSink.clearPending();
        primingScans = PRIMING_SCANS;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SeenKeys.clear();
        FtbWaypointSink.clearPending();
    }

    /**
     * 共通の登録判定: config → 重複判定（SeenKeys）→ Y 推定 → 登録層へ渡す、の順。
     * config が off の kind は SeenKeys にも記録しない（後で config を on にした時に効くように）。
     */
    private static void handleFound(Discovery.Kind kind, String id, Integer x, Integer z,
                                     ResourceKey<Level> dimension, Level level, boolean priming) {
        if (id == null || x == null || z == null) return;

        boolean enabled = kind == Discovery.Kind.STRUCTURE ? Config.STRUCTURES.get() : Config.BIOMES.get();
        if (!enabled) return;

        Discovery d = new Discovery(kind, id, x, z, dimension);
        if (!SeenKeys.add(d.key())) return;

        // ログイン時点で既に FOUND だったものは前のセッションの結果。記録だけして登録しない。
        if (priming) return;

        int y = YEstimator.estimate(level, x, z, id, kind == Discovery.Kind.BIOME);
        FtbWaypointSink.offer(d, y);
    }

    /**
     * Explorer's Compass の DataComponent 読み出しをここに閉じ込める。
     * EC が classpath に無い（未導入）場合は最初の呼び出しで {@link Throwable} を捕まえて
     * {@code available} を恒久的に false にし、以後は何もしない。
     */
    private static final class ECInner {
        private static volatile boolean available = true;

        static void tryHandle(ItemStack stack, Level level, ResourceKey<Level> dimension, boolean priming) {
            if (!available) return;

            String structureId;
            Integer x;
            Integer z;
            try {
                if (!(stack.getItem() instanceof com.chaosthedude.explorerscompass.items.ExplorersCompassItem)) {
                    return;
                }
                Integer state = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.COMPASS_STATE_COMPONENT);
                if (state == null || state != com.chaosthedude.explorerscompass.util.CompassState.FOUND.getID()) {
                    return;
                }
                structureId = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.STRUCTURE_ID_COMPONENT);
                x = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_X_COMPONENT);
                z = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_Z_COMPONENT);
            } catch (Throwable t) {
                available = false;
                CompassToMapFtb.LOGGER.warn(
                        "Explorer's Compass API mismatch or class missing. Structure detection disabled until restart: {}",
                        t.toString());
                return;
            }
            handleFound(Discovery.Kind.STRUCTURE, structureId, x, z, dimension, level, priming);
        }
    }

    /**
     * Nature's Compass の DataComponent 読み出しをここに閉じ込める。ECInner と同じ理由・同じ形。
     */
    private static final class NCInner {
        private static volatile boolean available = true;

        static void tryHandle(ItemStack stack, Level level, ResourceKey<Level> dimension, boolean priming) {
            if (!available) return;

            String biomeId;
            Integer x;
            Integer z;
            try {
                if (!(stack.getItem() instanceof com.chaosthedude.naturescompass.items.NaturesCompassItem)) {
                    return;
                }
                Integer state = stack.get(com.chaosthedude.naturescompass.NaturesCompass.COMPASS_STATE);
                if (state == null || state != com.chaosthedude.naturescompass.util.CompassState.FOUND.getID()) {
                    return;
                }
                biomeId = stack.get(com.chaosthedude.naturescompass.NaturesCompass.BIOME_ID);
                x = stack.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_X);
                z = stack.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_Z);
            } catch (Throwable t) {
                available = false;
                CompassToMapFtb.LOGGER.warn(
                        "Nature's Compass API mismatch or class missing. Biome detection disabled until restart: {}",
                        t.toString());
                return;
            }
            handleFound(Discovery.Kind.BIOME, biomeId, x, z, dimension, level, priming);
        }
    }
}
