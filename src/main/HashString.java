package main;

public class HashString {
    private final static int MAX_LEN = 50;
    private final static long P = 31;
    private final static long[] POWER_TABLE = new long[MAX_LEN + 1];

    private final long[] hashTable;

    static {
        POWER_TABLE[0] = 1;
        for (int i = 0; i < MAX_LEN; i++) {
            POWER_TABLE[i + 1] = POWER_TABLE[i] * P;
        }
    }

    HashString(CharSequence value) {
        int len = value.length();
        hashTable = new long[len + 1];
        for (int i = 0; i < len; i++) {
            hashTable[i + 1] = hashTable[i] * P + value.charAt(i);
        }
    }

    long hash(int begin, int end) {
        return hashTable[end] - hashTable[begin] * POWER_TABLE[end - begin];
    }

    static long hash(final String value) {
        long hash = 0;
        for (char c : value.toCharArray()) {
            hash *= P;
            hash += c;
        }

        return hash;
    }
}
