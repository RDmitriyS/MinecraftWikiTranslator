package main;

import java.util.TreeMap;

public class Node {
    protected final TreeMap<String, Word> en, ru;

    Node() {
        en = new TreeMap<>();
        ru = new TreeMap<>();
    }
}
