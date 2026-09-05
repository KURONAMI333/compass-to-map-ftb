package com.kuronami.compasstomapftb.client;

import com.kuronami.compasstomapftb.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** waypoint を登録した時だけ、本人のクライアントにチャット1行を出す（SPEC F8）。 */
public final class Notifier {

    private Notifier() {}

    /**
     * @param name 登録した waypoint の表示名（prettify 済み）
     * @param x    発見地点の X
     * @param z    発見地点の Z
     */
    public static void added(String name, int x, int z) {
        if (!Config.CHAT_NOTIFICATION.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.displayClientMessage(
                Component.translatable("message.compasstomapftb.added", name, x, z), false);
    }
}
