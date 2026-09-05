package com.kuronami.compasstomapftb;

import com.kuronami.compasstomapftb.client.YEstimator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link YEstimator#fallback} の全分岐を検査する。{@code estimate} は {@code Level} を
 * 要求するため Minecraft のランタイムが無いこのテストからは呼べない。
 */
class YEstimatorTest {

    @Test
    void theEnd() {
        assertEquals(64, YEstimator.fallback("minecraft:the_end", "minecraft:end_city", false));
    }

    @Test
    void theNether() {
        assertEquals(96, YEstimator.fallback("minecraft:the_nether", "minecraft:bastion_remnant", false));
    }

    @Test
    void biomeInOverworldFallsBackTo96() {
        assertEquals(96, YEstimator.fallback("minecraft:overworld", "minecraft:desert", true));
    }

    @Test
    void mineshaftIsUnderground() {
        assertEquals(40, YEstimator.fallback("minecraft:overworld", "minecraft:mineshaft", false));
    }

    @Test
    void dungeonIsUnderground() {
        assertEquals(40, YEstimator.fallback("minecraft:overworld", "somemod:dungeon", false));
    }

    @Test
    void strongholdIsUnderground() {
        assertEquals(40, YEstimator.fallback("minecraft:overworld", "minecraft:stronghold", false));
    }

    @Test
    void ancientCityIsUnderground() {
        assertEquals(40, YEstimator.fallback("minecraft:overworld", "minecraft:ancient_city", false));
    }

    @Test
    void trialChambersIsUnderground() {
        assertEquals(40, YEstimator.fallback("minecraft:overworld", "minecraft:trial_chambers", false));
    }

    @Test
    void oceanMonumentIsOceanic() {
        assertEquals(80, YEstimator.fallback("minecraft:overworld", "minecraft:ocean_monument", false));
    }

    @Test
    void shipwreckIsOceanic() {
        assertEquals(80, YEstimator.fallback("minecraft:overworld", "minecraft:shipwreck", false));
    }

    @Test
    void buriedTreasureIsOceanic() {
        assertEquals(80, YEstimator.fallback("minecraft:overworld", "minecraft:buried_treasure", false));
    }

    @Test
    void otherStructureFallsBackTo96() {
        assertEquals(96, YEstimator.fallback("minecraft:overworld", "minecraft:village_plains", false));
    }
}
