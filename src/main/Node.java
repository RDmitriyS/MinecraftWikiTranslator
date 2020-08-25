package main;

import java.util.TreeMap;

public class Node {
    protected final TreeMap<String, Word> en, ru;
    protected final TreeMap<Long, Word> hash_en;

    Node() {
        en = new TreeMap<>();
        ru = new TreeMap<>();
        hash_en = new TreeMap<>();
    }
}
