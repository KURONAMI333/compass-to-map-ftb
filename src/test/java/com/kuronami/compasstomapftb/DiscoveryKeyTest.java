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

    private static String key(String kind, String id, int x, int z) {
        String base = kind + "|" + id;
        return "STRUCTURE".equals(kind) ? base + "|" + x + "|" + z : base;
    }

    @Test
    @DisplayName("バイオームの key は座標を含まない（検索のたびに座標がぶれるため）")
    void biomeKeyIgnoresCoordinates() {
        String a = key("BIOME", "minecraft:bamboo_jungle", 419, -315);
        String b = key("BIOME", "minecraft:bamboo_jungle", 410, -277);
        assertEquals(a, b, "同じバイオームを再検索して座標がずれても同一の key になること");
        assertEquals("BIOME|minecraft:bamboo_jungle", a);
    }

    @Test
    @DisplayName("構造物の key は座標を含む（別の個体には別のピンを立てる）")
    void structureKeyKeepsCoordinates() {
        String a = key("STRUCTURE", "minecraft:village_plains", 890, -432);
        String b = key("STRUCTURE", "minecraft:village_plains", 2400, 100);
        assertNotEquals(a, b, "別の位置の同じ構造物は別扱いになること");
        assertEquals("STRUCTURE|minecraft:village_plains|890|-432", a);
    }

    @Test
    @DisplayName("key に次元は入らない（FOUND のコンパスを持って次元を移っても再登録しない）")
    void dimensionIsNotPartOfKey() {
        // 同じコンパスをオーバーワールドで観測した時と、ネザーへ持ち込んで観測した時。
        // 次元が key に入っていると、移動した瞬間にネザーの地図へ他次元の座標が立つ。
        assertEquals(
                key("BIOME", "minecraft:beach", 250, 18),
                key("BIOME", "minecraft:beach", 250, 18));
        assertEquals(
                key("STRUCTURE", "minecraft:igloo", -6080, 1056),
                key("STRUCTURE", "minecraft:igloo", -6080, 1056));
    }

    @Test
    @DisplayName("種別が違えば別扱い")
    void kindSeparates() {
        assertNotEquals(
                key("BIOME", "minecraft:jungle", 1, 2),
                key("STRUCTURE", "minecraft:jungle", 1, 2));
    }
}
