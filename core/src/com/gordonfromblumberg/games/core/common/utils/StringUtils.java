package com.gordonfromblumberg.games.core.common.utils;

public class StringUtils {
    static final String PLACE_HOLDER = "#";
    private static final StringBuilder sb = new StringBuilder();

    private StringUtils() {}

    public static String format(String template, Object... args) {
        if (args == null || args.length == 0) {
            if (template.contains(PLACE_HOLDER)) {
                throw new IllegalArgumentException("No arguments passed for template '" + template + "'");
            } else {
                return template;
            }
        }

        String[] parts = template.split(PLACE_HOLDER, -1);
        if (parts.length - 1 != args.length) {
            throw new IllegalArgumentException("Incorrect number of arguments: " + args.length
                    + " passed for template '" + template + "'");
        }

        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 0; i < args.length; i++) {
            result.append(args[i]).append(parts[i + 1]);
        }

        return result.toString();
    }

    public static String padLeft(long number, int pad) {
        try {
            String numberStr = String.valueOf(number);
            int length = Math.max(numberStr.length(), pad);
            sb.ensureCapacity(length);
            if (pad > numberStr.length()) {
                int zeros = pad - numberStr.length();
                while (zeros-- > 0)
                    sb.append('0');
            }
            sb.append(numberStr);
            return sb.toString();
        } finally {
            sb.delete(0, sb.length());
        }
    }
}
