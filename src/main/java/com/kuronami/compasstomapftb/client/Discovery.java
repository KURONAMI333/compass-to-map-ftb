package com.kuronami.compasstomapftb.client;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * コンパスが FOUND を返した1件の発見。検出層（CompassScanner）から
 * 登録層（FtbWaypointSink）へ渡す唯一の型。
 *
 * @param kind      発見の種別（STRUCTURE = Explorer's Compass / BIOME = Nature's Compass）
 * @param id        対象の ResourceLocation 文字列（例 "minecraft:village_plains"）
 * @param x         発見地点の X
 * @param z         発見地点の Z
 * @param dimension 発見時にプレイヤーが居た次元
 */
public record Discovery(Kind kind, String id, int x, int z, ResourceKey<Level> dimension) {

    public enum Kind { STRUCTURE, BIOME }

    /**
     * セッション内の重複判定に使う key。Y は含めない
     * （Y はチャンクのロード状況に依存して変わるため。FtbWaypointSink の
     * 既存 waypoint 照合も同じ理由で x/z/dimension だけを見る）。
     */
    public String key() {
        return dimension.location() + "|" + kind + "|" + id + "|" + x + "|" + z;
    }
}
