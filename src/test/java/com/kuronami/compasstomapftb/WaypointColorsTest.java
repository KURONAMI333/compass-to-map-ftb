package com.kuronami.compasstomapftb;

import com.kuronami.compasstomapftb.compat.ftb.WaypointColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link WaypointColors} の文字列 → int 検査。Minecraft のクラスには触れない
 * （{@code forStructure} / {@code forBiome} はどちらも String を受け取り int を返すだけ）。
 */
class WaypointColorsTest {

    @Test
    void village() {
        assertEquals(0xFFD700, WaypointColors.forStructure("minecraft:village_plains"));
    }

    @Test
    void stronghold() {
        assertEquals(0x9B59B6, WaypointColors.forStructure("minecraft:stronghold"));
    }

    @Test
    void uncategorizedVanillaStructure() {
        // vanilla namespace かつどのキーワードにもヒットしない → 白
        assertEquals(0xFFFFFF, WaypointColors.forStructure("minecraft:nether_fossil"));
    }

    @Test
    void modStructureGetsHashColor() {
        int color = WaypointColors.forStructure("somemod:weird_ruin");
        assertNotEquals(0xFFFFFF, color);
        // 決定的（同じ入力なら常に同じ色）
        assertEquals(color, WaypointColors.forStructure("somemod:weird_ruin"));
    }

    @Test
    void desertBiome() {
        assertEquals(0xF5DEB3, WaypointColors.forBiome("minecraft:desert"));
    }

    @Test
    void forestBiome() {
        assertEquals(0x228B22, WaypointColors.forBiome("minecraft:forest"));
    }

    @Test
    void cherryGrovePrefersIndividualKeywordOverGeneric() {
        // "grove" (forest 系) より "cherry" を優先する
        assertEquals(0xFFB6C1, WaypointColors.forBiome("minecraft:cherry_grove"));
    }

    @Test
    void crimsonForestPrefersIndividualKeywordOverGeneric() {
        // "forest" より "crimson" (ネザー系) を優先する
        assertEquals(0xCC3333, WaypointColors.forBiome("minecraft:crimson_forest"));
    }

    @Test
    void uncategorizedVanillaBiome() {
        // "void" 自体が end/void グループのキーワードなので、どのキーワードにも
        // ヒットしない架空の minecraft namespace 文字列で未分類判定を確認する。
        assertEquals(0xFFFFFF, WaypointColors.forBiome("minecraft:nonexistent_biome_xyz"));
    }

    @Test
    void modBiomeGetsHashColor() {
        int color = WaypointColors.forBiome("somemod:weird_biome");
        assertNotEquals(0xFFFFFF, color);
        assertEquals(color, WaypointColors.forBiome("somemod:weird_biome"));
    }
}
