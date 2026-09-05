package com.kuronami.compasstomapftb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * G1 ゲート専用の使い捨てスパイク（サーバー側）。ゲート通過後に削除する。
 *
 * <p>ログインしたプレイヤーに、FOUND の component を既に持った NC / EC のコンパスを配る。
 * component を書くのはサーバーなので、クライアント側の {@code G1Spike} がそれを読めれば
 * 「vanilla の ItemStack 同期だけでクライアントが検索結果を読める」ことの実測になる
 * （これが G1 の問い。実際の検索でも component を書くのはサーバー）。
 */
@EventBusSubscriber(modid = CompassToMapFtb.MODID)
public final class G1SpikeServer {
    private static final Logger LOG = LoggerFactory.getLogger("C2M-FTB/G1");

    private G1SpikeServer() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        giveNc(player);
        giveEc(player);
    }

    private static void giveNc(ServerPlayer player) {
        try {
            ItemStack stack = new ItemStack(com.chaosthedude.naturescompass.NaturesCompass.naturesCompass);
            stack.set(com.chaosthedude.naturescompass.NaturesCompass.COMPASS_STATE,
                    com.chaosthedude.naturescompass.util.CompassState.FOUND.getID());
            stack.set(com.chaosthedude.naturescompass.NaturesCompass.BIOME_ID, "minecraft:desert");
            stack.set(com.chaosthedude.naturescompass.NaturesCompass.FOUND_X, 1234);
            stack.set(com.chaosthedude.naturescompass.NaturesCompass.FOUND_Z, -567);
            player.getInventory().add(stack);
            LOG.info("G1 SERVER gave NC compass with state=2 x=1234 z=-567");
        } catch (Throwable t) {
            LOG.warn("G1 SERVER could not give NC compass: {}", t.toString());
        }
    }

    private static void giveEc(ServerPlayer player) {
        try {
            ItemStack stack = new ItemStack(com.chaosthedude.explorerscompass.ExplorersCompass.explorersCompass);
            stack.set(com.chaosthedude.explorerscompass.ExplorersCompass.COMPASS_STATE_COMPONENT,
                    com.chaosthedude.explorerscompass.util.CompassState.FOUND.getID());
            stack.set(com.chaosthedude.explorerscompass.ExplorersCompass.STRUCTURE_ID_COMPONENT,
                    "minecraft:village_plains");
            stack.set(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_X_COMPONENT, 890);
            stack.set(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_Z_COMPONENT, -432);
            player.getInventory().add(stack);
            LOG.info("G1 SERVER gave EC compass with state=2 x=890 z=-432");
        } catch (Throwable t) {
            LOG.warn("G1 SERVER could not give EC compass: {}", t.toString());
        }
    }
}
