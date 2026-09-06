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
     * 重複判定の key を組み立てる。**種別で座標の扱いが違う**（理由は {@link Discovery#key()}）。
     *
     * <p>{@link Discovery} は {@code ResourceKey<Level>} を持つため JUnit から組み立てられない。
     * 規則そのものはここに置いて Minecraft のクラスに触れない形にし、{@link Discovery#key()} は
     * これを呼ぶだけにする。**テストはこのメソッドを直接叩く**（規則を変えたらテストが落ちる）。
     *
     * @param kind {@link Discovery.Kind} の名前（{@code "STRUCTURE"} / {@code "BIOME"}）
     */
    public static String keyOf(String kind, String id, int x, int z) {
        String base = kind + "|" + id;
        return "STRUCTURE".equals(kind) ? base + "|" + x + "|" + z : base;
    }

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

    /**
     * 記録を取り消す。登録に失敗して発見を捨てた時に呼ぶ。
     *
     * <p>これが無いと、保留キューのタイムアウトで捨てられた発見の key が残り続け、
     * コンパスが FOUND のまま手元にあってもそのセッション中は二度と登録されない
     * （症状が「何も起きない」なので、利用者からは原因の分からない不具合に見える）。
     */
    public static synchronized void remove(String key) {
        SEEN.remove(key);
    }

    /** ログアウト時に全部忘れる。 */
    public static synchronized void clear() {
        SEEN.clear();
    }
}
