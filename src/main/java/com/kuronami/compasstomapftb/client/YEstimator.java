package com.kuronami.compasstomapftb.client;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * waypoint に使う Y 座標の推定。コンパスの検索結果は数千ブロック先が普通で、発見時点では
 * そのチャンクがクライアントにロードされていないことが多い。{@link #estimate} はまず
 * heightmap を見て、意味のある値が取れなければ {@link #fallback} の安全な既定値へ落とす
 * （適当な Y のまま登録すると奈落や地中にピンが立つ）。
 *
 * <p>C2M（mod-003）の {@code CompassWatcher#estimateY} の移植。{@link #fallback} は
 * Minecraft のクラスに触れない純関数にして JUnit で直接検証する。
 */
public final class YEstimator {

    private YEstimator() {}

    /**
     * クライアントの heightmap を見て Y を推定する。チャンク未ロード等で
     * {@code level.getMinBuildHeight() + 1} 以下しか返らない場合は {@link #fallback} へ委譲する。
     */
    public static int estimate(Level level, int x, int z, String resourceId, boolean isBiome) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 1) {
            return fallback(level.dimension().location().toString(), resourceId, isBiome);
        }
        return y;
    }

    /** Minecraft に触れない純関数。dimension / 種別ごとの安全な Y を返す。 */
    public static int fallback(String dimensionId, String resourceId, boolean isBiome) {
        if ("minecraft:the_end".equals(dimensionId)) return 64;
        if ("minecraft:the_nether".equals(dimensionId)) return 96;
        if (isBiome) return 96;

        String lower = resourceId == null ? "" : resourceId.toLowerCase();
        if (lower.contains("mineshaft") || lower.contains("dungeon")
                || lower.contains("stronghold") || lower.contains("ancient_city")
                || lower.contains("trial_chambers")) {
            return 40;
        }
        if (lower.contains("ocean_monument") || lower.contains("shipwreck")
                || lower.contains("buried_treasure")) {
            return 80;
        }
        return 96;
    }
}
