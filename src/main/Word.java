package main;

import java.util.Comparator;

public class Word implements Comparable<Word> {
    final String name;
    final int pos;
    final int section;
    final boolean outdated;

    Word(String name, int pos, int section, boolean outdated) {
        this.name = name;
        this.pos = pos;
        this.section = section;
        this.outdated = outdated;
    }

    Word(String name, int pos, int section) {
        this(name, pos, section, false);
    }

    Word(String name, int pos) {
        this(name, pos, -1);
    }

    @Override
    public int compareTo(final Word word) {
        return name.compareTo(word.name);
    }

    final static Comparator<Word> nameComparator =
            Comparator.comparing((Word w) -> w.name).thenComparingInt(w -> w.pos);

    final static Comparator<Word> posComparator =
            Comparator.comparingInt((Word w) -> w.pos).thenComparing(w -> w.name);
}
