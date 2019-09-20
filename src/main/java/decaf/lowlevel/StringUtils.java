package decaf.lowlevel;

public final class StringUtils {
    /**
     * Quote an unquoted string.
     *
     * @param str unquoted string
     * @return quoted string
     */
    public static String quote(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\\' -> sb.append("\\\\");
                default -> sb.append(c);
            }
        }
        return ('"' + sb.toString() + '"');
    }
}
