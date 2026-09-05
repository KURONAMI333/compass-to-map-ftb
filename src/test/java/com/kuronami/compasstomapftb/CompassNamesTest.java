package com.kuronami.compasstomapftb;

import com.kuronami.compasstomapftb.client.CompassNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link CompassNames#prettify} の代表例を検査する。Minecraft のクラスには触れない。
 */
class CompassNamesTest {

    @Test
    void villagePlains() {
        assertEquals("Village Plains", CompassNames.prettify("minecraft:village_plains"));
    }

    @Test
    void cherryGrove() {
        assertEquals("Cherry Grove", CompassNames.prettify("naturescompass:cherry_grove"));
    }

    @Test
    void nullReturnsUnknown() {
        assertEquals("Unknown", CompassNames.prettify(null));
    }

    @Test
    void emptyStringReturnsUnknown() {
        assertEquals("Unknown", CompassNames.prettify(""));
    }

    @Test
    void noColonUsesWholeStringAsPath() {
        assertEquals("Just A Path", CompassNames.prettify("just_a_path"));
    }
}
