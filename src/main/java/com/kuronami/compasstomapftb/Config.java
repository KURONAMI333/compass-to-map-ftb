package com.kuronami.compasstomapftb;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * client config。項目は3つだけ（SPEC §5）。
 *
 * <p>色・名前の書式・Y 推定・保留の待機時間は意図的に出していない。「入れただけで効く。
 * 既定値が製品」であり、色と名前は登録後に FTB Chunks 側の編集画面で個別に変えられるため、
 * こちらに設定を置くと二重管理になる。項目を足したくなったら SPEC §5 を先に直すこと。
 */
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue STRUCTURES = BUILDER
            .comment("Add a waypoint when Explorer's Compass finds a structure")
            .translation("compasstomapftb.config.structures")
            .define("structures", true);

    public static final ModConfigSpec.BooleanValue BIOMES = BUILDER
            .comment("Add a waypoint when Nature's Compass finds a biome")
            .translation("compasstomapftb.config.biomes")
            .define("biomes", true);

    public static final ModConfigSpec.BooleanValue CHAT_NOTIFICATION = BUILDER
            .comment("Show a chat line when a waypoint is added")
            .translation("compasstomapftb.config.chatNotification")
            .define("chatNotification", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}
}
