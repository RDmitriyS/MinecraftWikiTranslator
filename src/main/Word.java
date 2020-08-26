package main;

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

    @Override
    public int compareTo(final Word word) {
        return Functions.compareTo(name, word.name);
    }

    public int compareByName(final Word word) {
        final int temp = compareTo(word);
        return temp != 0 ? temp : Integer.compare(pos, word.pos);
    }

    public int compareByPos(final Word word) {
        final int temp = Integer.compare(pos, word.pos);
        return temp != 0 ? temp : compareTo(word);
    }
}
