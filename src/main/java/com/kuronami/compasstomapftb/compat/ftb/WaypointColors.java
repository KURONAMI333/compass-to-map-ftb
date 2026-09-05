package com.kuronami.compasstomapftb.compat.ftb;

import com.kuronami.compasstomapftb.client.Discovery;

/**
 * 発見の種別 / ID から waypoint の色を決める。
 *
 * <p>mod-003（JourneyMap 版・{@code compat/jm/JourneyMapClientHook}）の
 * {@code colorByCategory} / {@code colorByBiome} の写し。分類の閾値・色値は変えていない。
 * JourneyMap 版が持っていた config トグル（{@code COLOR_BY_CATEGORY} 無効時の単色化）は
 * このMODの Config に存在しないため持ち込まない。
 *
 * <p>namespace の抽出は {@code net.minecraft.resources.ResourceLocation} を使わず、文字列の
 * {@code ':'} 分割だけで行う（このクラスは Minecraft のクラスに一切触れない。JUnit だけで
 * 動く {@code WaypointColorsTest} が Minecraft ランタイムを必要とせずに検証できるようにするため）。
 */
public final class WaypointColors {

    private WaypointColors() {}

    /** 未分類の構造物 / バイオームに使う色。 */
    private static final int UNCATEGORIZED = 0xFFFFFF;

    /** {@link Discovery} の種別に応じて {@link #forStructure(String)} / {@link #forBiome(String)} へ振り分ける。 */
    public static int forDiscovery(Discovery d) {
        return switch (d.kind()) {
            case STRUCTURE -> forStructure(d.id());
            case BIOME -> forBiome(d.id());
        };
    }

    /**
     * 構造物カテゴリ別色。
     *
     * 判定順:
     *  1. キーワード判定（vanilla / MOD 問わずカテゴリキーワード入りはここでヒット）
     *  2. namespace が minecraft 以外で未ヒット → ID から hash 色生成
     *  3. それ以外（バニラの未分類構造物） → 白
     */
    public static int forStructure(String structureId) {
        String lower = structureId.toLowerCase();
        if (lower.contains("village")) return 0xFFD700;             // 黄
        if (lower.contains("mineshaft") || lower.contains("dungeon")) return 0xCC3333; // 赤
        if (lower.contains("stronghold") || lower.contains("end_city")) return 0x9B59B6; // 紫
        if (lower.contains("ocean_monument")
                || lower.contains("temple")
                || lower.contains("pyramid")
                || lower.contains("swamp_hut")
                || lower.contains("igloo")) return 0x00CED1;        // シアン（テンプル系）
        if (lower.contains("fortress") || lower.contains("bastion")) return 0xFF8C00;   // オレンジ
        if (lower.contains("ruined_portal")) return 0x808080;       // グレー
        if (lower.contains("woodland_mansion")) return 0x8B4513;    // 茶
        if (lower.contains("ancient_city")) return 0x00FFFF;        // 水色
        if (lower.contains("trial_chambers")) return 0x32CD32;      // ライム
        if (lower.contains("shipwreck") || lower.contains("buried_treasure")) return 0xDAA520; // 山吹色（海賊系）

        // MOD 構造物 → hash 色
        if (!isVanillaNamespace(structureId)) {
            int hue = Math.floorMod(structureId.hashCode(), 360);
            return hslToRgb(hue, 0.7f, 0.55f);
        }

        return UNCATEGORIZED;
    }

    /**
     * バイオームカテゴリ別色。
     *
     * 判定順:
     *  1. 個別バイオーム判定（汎用キーワードと衝突するものを先に判定。例: cherry_grove は
     *     "grove" より "cherry" を優先、crimson_forest は "forest" より "crimson" を優先）
     *  2. 汎用カテゴリ判定
     *  3. namespace が minecraft 以外で未ヒット → hash 色（構造物より落ち着いた彩度）
     *  4. それ以外 → 白
     */
    public static int forBiome(String biomeId) {
        String lower = biomeId.toLowerCase();

        // ① 個別バイオーム判定（汎用キーワードと衝突するもの）
        if (lower.contains("cherry"))
            return 0xFFB6C1; // light pink (cherry_grove)
        if (lower.contains("crimson") || lower.contains("warped")
                || lower.contains("nether") || lower.contains("basalt")
                || lower.contains("soul_sand"))
            return 0xCC3333; // 赤（ネザー系: crimson_forest, warped_forest 等）
        if (lower.contains("deep_dark") || lower.contains("dripstone")
                || lower.contains("lush") || lower.contains("cave"))
            return 0x8B4513; // 茶（洞窟系: deep_dark, dripstone_caves 等）
        if (lower.contains("mushroom"))
            return 0xFF69B4; // pink (mushroom_fields)

        // ② 汎用カテゴリ判定
        if (lower.contains("desert") || lower.contains("badlands") || lower.contains("mesa"))
            return 0xF5DEB3; // wheat
        if (lower.contains("jungle"))
            return 0x2D5016; // 暗緑
        if (lower.contains("forest") || lower.contains("taiga") || lower.contains("birch")
                || lower.contains("grove") || lower.contains("woodland"))
            return 0x228B22; // forest green
        if (lower.contains("ocean") || lower.contains("river"))
            return 0x1E90FF; // dodger blue
        if (lower.contains("snow") || lower.contains("frozen") || lower.contains("ice"))
            return 0xF0FFFF; // azure
        if (lower.contains("mountain") || lower.contains("peak") || lower.contains("hill")
                || lower.contains("slope") || lower.contains("meadow"))
            return 0xA9A9A9; // dark gray
        if (lower.contains("end") || lower.contains("void"))
            return 0x9B59B6; // 紫
        if (lower.contains("plain") || lower.contains("savanna"))
            return 0x9ACD32; // yellow green
        if (lower.contains("beach") || lower.contains("shore"))
            return 0xFFE4B5; // moccasin
        if (lower.contains("swamp") || lower.contains("mangrove"))
            return 0x556B2F; // dark olive

        // MOD バイオーム → hash 色（構造物との視認区別のため saturation 低め）
        if (!isVanillaNamespace(biomeId)) {
            int hue = Math.floorMod(biomeId.hashCode(), 360);
            return hslToRgb(hue, 0.55f, 0.6f); // 構造物 (S=0.7, L=0.55) よりパステル寄り
        }

        return UNCATEGORIZED;
    }

    /**
     * {@code "namespace:path"} 形式の文字列から namespace を取り出し、{@code "minecraft"} かどうかを
     * 判定する。コロンが無い / 先頭がコロンの不正な ID は vanilla 扱い（hash 色に回さない）にする。
     */
    private static boolean isVanillaNamespace(String resourceId) {
        int colon = resourceId.indexOf(':');
        String namespace = colon <= 0 ? "minecraft" : resourceId.substring(0, colon);
        return "minecraft".equals(namespace);
    }

    /** HSL → RGB 変換。saturation/lightness 固定で揃えると鮮明で見やすい色になる。 */
    private static int hslToRgb(float h, float s, float l) {
        float c = (1f - Math.abs(2f * l - 1f)) * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = l - c / 2f;

        float r, g, b;
        if (h < 60)       { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else              { r = c; g = 0; b = x; }

        int ri = clamp255(Math.round((r + m) * 255));
        int gi = clamp255(Math.round((g + m) * 255));
        int bi = clamp255(Math.round((b + m) * 255));
        return (ri << 16) | (gi << 8) | bi;
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
