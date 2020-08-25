package main;

public class HashString {
    private final static int maxLen = 50;
    private final static long p = 31;
    private final static long[] p_table = new long[maxLen + 1];

    private final long[] hash_table;

    static {
        p_table[0] = 1;
        for (int i = 0; i < maxLen; i++) {
            p_table[i + 1] = p_table[i] * p;
        }
    }

    HashString(final String value) {
        int len = value.length();
        hash_table = new long[len + 1];
        for (int i = 0; i < len; i++) {
            hash_table[i + 1] = hash_table[i] * p + value.charAt(i);
        }
    }

    long hash(int begin, int end) {
        return hash_table[end] - hash_table[begin] * p_table[end - begin];
    }

    static long hash(final String value) {
        long hash = 0;
        for (final char c : value.toCharArray()) {
            hash *= p;
            hash += c;
        }

        return hash;
    }
}
