package com.kuronami.compasstomapftb.client;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * セッション内の重複登録防止。{@link Discovery#key()} を記録するだけの純 Java クラスで、
 * Minecraft のクラスには一切触れない（JUnit で直接テストするため）。
 *
 * <p>C2M（mod-003）の {@code CompassWatcher#recordSeen} と同じ LRU 形（{@link LinkedHashSet}
 * + 上限超過で最古を1件捨てる）だが、C2M はプレイヤーごとに {@code Map<UUID, Set<String>>} を
 * 持つのに対し、こちらはクライアント側でログインプレイヤーが常に1人なので単一 {@link Set} でよい。
 */
public final class SeenKeys {

    private static final int MAX_SIZE = 512;

    private static final Set<String> SEEN = new LinkedHashSet<>();

    private SeenKeys() {}

    /**
     * 初めて見た key なら記録して {@code true} を返す。既に見ていれば {@code false}。
     * 上限 {@value #MAX_SIZE} 件を超えたら、挿入順で最も古いものを1件捨てる。
     */
    public static synchronized boolean add(String key) {
        if (!SEEN.add(key)) return false;
        if (SEEN.size() > MAX_SIZE) {
            Iterator<String> it = SEEN.iterator();
            it.next();
            it.remove();
        }
        return true;
    }

    /** ログアウト時に全部忘れる。 */
    public static synchronized void clear() {
        SEEN.clear();
    }
}
