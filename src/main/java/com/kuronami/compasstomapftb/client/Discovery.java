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
     * セッション内の重複判定に使う key。**種別で座標の扱いが違う。**
     *
     * <p><b>BIOME は座標を含めない。</b> Nature's Compass はプレイヤーの現在地を起点に
     * {@code sampleSpace}（既定 64 ブロック）刻みの格子でサンプリングし、最初に当たった点を返す
     * （{@code BiomeSearchWorker}）。起点が動けば格子ごとずれるので、<b>同じバイオームでも
     * 検索のたびに違う座標が返るのが正常</b>。実測でも同じ Bamboo Jungle が
     * (419,-315) → (415,-279) → (410,-277) と返った。座標を key に入れると、
     * 再検索のたびに数十ブロック隣へピンが増える。
     *
     * <p><b>STRUCTURE は座標を含める。</b> Explorer's Compass が返すのは
     * {@code placement.getLocatePos(structureStart.getChunkPos())}＝その構造物の実位置
     * （{@code StructureSearchWorker#succeed}）なので、同じ構造物なら常に同じ座標になる。
     * 座標を含めておけば、別の村を見つけた時にちゃんと別のピンが立つ。
     *
     * <p>Y はどちらにも含めない（チャンクのロード状況で変わるため）。
     */
    public String key() {
        String base = dimension.location() + "|" + kind + "|" + id;
        return kind == Kind.STRUCTURE ? base + "|" + x + "|" + z : base;
    }
}
