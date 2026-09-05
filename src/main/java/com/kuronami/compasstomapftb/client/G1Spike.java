package com.kuronami.compasstomapftb.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * G1 ゲート専用の使い捨てスパイク。client の tick で自分のインベントリのコンパスから
 * compass_state / found_x / found_z が読めるかだけを見る。ゲート通過後に削除する。
 */
@EventBusSubscriber(modid = "compasstomapftb", value = Dist.CLIENT)
public final class G1Spike {
    private static final Logger LOG = LoggerFactory.getLogger("C2M-FTB/G1");
    private static int tick = 0;
    private static String lastLine = "";

    private G1Spike() {}

    /** dev run でのみ true。ゲート検証が済んだらこのクラスごと消す。 */
    private static final boolean ENABLED = Boolean.getBoolean("compasstomapftb.g1spike");

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED) return;
        if (++tick % 20 != 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            String ec = ECProbe.describe(stack);
            if (ec != null) sb.append("[EC slot=").append(i).append(' ').append(ec).append(']');
            String nc = NCProbe.describe(stack);
            if (nc != null) sb.append("[NC slot=").append(i).append(' ').append(nc).append(']');
        }
        String line = sb.toString();
        if (!line.isEmpty() && !line.equals(lastLine)) {
            lastLine = line;
            LOG.info("G1 PROBE {}", line);
        }
    }

    /** EC のクラス参照をここに閉じ込める（不在時に本体を巻き込まない）。 */
    private static final class ECProbe {
        private static boolean available = true;

        static String describe(ItemStack stack) {
            if (!available) return null;
            try {
                if (!(stack.getItem() instanceof com.chaosthedude.explorerscompass.items.ExplorersCompassItem)) {
                    return null;
                }
                Integer state = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.COMPASS_STATE_COMPONENT);
                String id = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.STRUCTURE_ID_COMPONENT);
                Integer x = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_X_COMPONENT);
                Integer z = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_Z_COMPONENT);
                return "state=" + state + " id=" + id + " x=" + x + " z=" + z
                        + " FOUND=" + com.chaosthedude.explorerscompass.util.CompassState.FOUND.getID();
            } catch (Throwable t) {
                available = false;
                LOG.warn("G1 EC probe disabled: {}", t.toString());
                return null;
            }
        }
    }

    /** NC のクラス参照をここに閉じ込める。 */
    private static final class NCProbe {
        private static boolean available = true;

        static String describe(ItemStack stack) {
            if (!available) return null;
            try {
                if (!(stack.getItem() instanceof com.chaosthedude.naturescompass.items.NaturesCompassItem)) {
                    return null;
                }
                Integer state = stack.get(com.chaosthedude.naturescompass.NaturesCompass.COMPASS_STATE);
                String id = stack.get(com.chaosthedude.naturescompass.NaturesCompass.BIOME_ID);
                Integer x = stack.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_X);
                Integer z = stack.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_Z);
                return "state=" + state + " id=" + id + " x=" + x + " z=" + z
                        + " FOUND=" + com.chaosthedude.naturescompass.util.CompassState.FOUND.getID();
            } catch (Throwable t) {
                available = false;
                LOG.warn("G1 NC probe disabled: {}", t.toString());
                return null;
            }
        }
    }
}
