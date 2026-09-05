package com.kuronami.compasstomapftb.client;

/**
 * 構造物 / バイオーム ID を waypoint の表示名に変換する。
 * C2MX（mod-025）の {@code util/CompassNames#prettify} の写し。
 */
public final class CompassNames {

    private CompassNames() {}

    /**
     * {@code "minecraft:village_plains"} → {@code "Village Plains"}.
     * {@code "naturescompass:cherry_grove"} → {@code "Cherry Grove"}.
     * namespace は捨てて path だけを Title Case 化する。
     */
    public static String prettify(String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) return "Unknown";
        int colon = resourceId.indexOf(':');
        String path = colon < 0 ? resourceId : resourceId.substring(colon + 1);
        StringBuilder sb = new StringBuilder(path.length());
        boolean cap = true;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '_' || c == '/') {
                sb.append(' ');
                cap = true;
            } else if (cap) {
                sb.append(Character.toUpperCase(c));
                cap = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
