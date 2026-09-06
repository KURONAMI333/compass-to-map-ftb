package com.kuronami.compasstomapftb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code Discovery#key()} の種別ごとの座標の扱いを、Minecraft のクラスに触れずに検証する。
 *
 * <p>{@code Discovery} は {@code ResourceKey<Level>} を持つので、そのままでは JUnit から
 * 組み立てられない。key の組み立て規則だけを同じ形で書き写して検証する
 * （規則を変えたらこのテストも落ちるように、期待値は文字列で直書きする）。
 */
class DiscoveryKeyTest {

    private static String key(String dimension, String kind, String id, int x, int z) {
        String base = dimension + "|" + kind + "|" + id;
        return "STRUCTURE".equals(kind) ? base + "|" + x + "|" + z : base;
    }

    @Test
    @DisplayName("バイオームの key は座標を含まない（検索のたびに座標がぶれるため）")
    void biomeKeyIgnoresCoordinates() {
        String a = key("minecraft:overworld", "BIOME", "minecraft:bamboo_jungle", 419, -315);
        String b = key("minecraft:overworld", "BIOME", "minecraft:bamboo_jungle", 410, -277);
        assertEquals(a, b, "同じバイオームを再検索して座標がずれても同一の key になること");
        assertEquals("minecraft:overworld|BIOME|minecraft:bamboo_jungle", a);
    }

    @Test
    @DisplayName("構造物の key は座標を含む（別の個体には別のピンを立てる）")
    void structureKeyKeepsCoordinates() {
        String a = key("minecraft:overworld", "STRUCTURE", "minecraft:village_plains", 890, -432);
        String b = key("minecraft:overworld", "STRUCTURE", "minecraft:village_plains", 2400, 100);
        assertNotEquals(a, b, "別の位置の同じ構造物は別扱いになること");
        assertEquals("minecraft:overworld|STRUCTURE|minecraft:village_plains|890|-432", a);
    }

    @Test
    @DisplayName("次元が違えば別扱い")
    void dimensionSeparates() {
        assertNotEquals(
                key("minecraft:overworld", "BIOME", "minecraft:desert", 1, 2),
                key("minecraft:the_nether", "BIOME", "minecraft:desert", 1, 2));
    }

    @Test
    @DisplayName("種別が違えば別扱い")
    void kindSeparates() {
        assertNotEquals(
                key("minecraft:overworld", "BIOME", "minecraft:jungle", 1, 2),
                key("minecraft:overworld", "STRUCTURE", "minecraft:jungle", 1, 2));
    }
}
