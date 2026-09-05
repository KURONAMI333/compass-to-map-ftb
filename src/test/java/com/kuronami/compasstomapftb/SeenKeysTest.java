package com.kuronami.compasstomapftb;

import com.kuronami.compasstomapftb.client.SeenKeys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SeenKeys} の重複判定・LRU 上限を検査する。Minecraft のクラスには触れない。
 */
class SeenKeysTest {

    @AfterEach
    void cleanup() {
        SeenKeys.clear();
    }

    @Test
    void firstSeenReturnsTrue() {
        assertTrue(SeenKeys.add("overworld|STRUCTURE|minecraft:village_plains|100|200"));
    }

    @Test
    void secondSeenReturnsFalse() {
        String key = "overworld|BIOME|minecraft:desert|10|20";
        assertTrue(SeenKeys.add(key));
        assertFalse(SeenKeys.add(key));
    }

    @Test
    void afterClearSameKeyIsFreshAgain() {
        String key = "overworld|STRUCTURE|minecraft:stronghold|0|0";
        assertTrue(SeenKeys.add(key));
        SeenKeys.clear();
        assertTrue(SeenKeys.add(key));
    }

    @Test
    void oldestEntryDroppedWhenOverCapacity() {
        // 上限は512件。513件目を入れると1件目（最古）が捨てられ、再登録できるようになる。
        for (int i = 0; i < 512; i++) {
            assertTrue(SeenKeys.add("key-" + i));
        }
        // 513件目を追加 → 最古の "key-0" が捨てられる。
        assertTrue(SeenKeys.add("key-512"));

        // 最古の key-0 はもう記憶されていないので、再度 add すると true（＝初回扱い）に戻る。
        // （この再 add 自体が514件目の挿入になり key-1 を新たに最古として捨てるので、
        //  この後で key-1 を検査してはいけない — 検査は別テストに分ける）
        assertTrue(SeenKeys.add("key-0"));
    }

    @Test
    void recentEntrySurvivesSingleEviction() {
        // 上限を1件超えても、直近に追加した key はまだ残っている（捨てられるのは常に最古の1件だけ）。
        for (int i = 0; i < 512; i++) {
            SeenKeys.add("key-" + i);
        }
        SeenKeys.add("key-512"); // 最古の key-0 だけが捨てられる。

        assertFalse(SeenKeys.add("key-511"));
    }
}
