package main;

import java.util.*;
import java.util.function.Function;

import static java.lang.Character.isLetter;
import static main.Functions.*;

public class Table extends Node {
    private final List<Node> data;
    private final TreeMap<Long, Word> hash_en;

    private final TableType type;
    private final int maxID;

    Table(TableType type, int maxID) {
        this.type = type;
        this.maxID = maxID;
        data = new ArrayList<>(maxID);
        for (int i = 0; i < maxID; i++) {
            data.add(new Node());
        }

        hash_en = new TreeMap<>();
    }

    private Node get(int index) {
        return data.get(index);
    }

    private void add(final Word word, Lang lang) {
        if (lang == Lang.ru) {
            add_ru(word);
        } else {
            add_en(word);
        }
    }

    private void add_en(final Word word_en) {
        if (type != TableType.Default && en.get(word_en.name) != null) {
            System.out.println("[" + word_en.name + "] already exist");
        }

        en.put(word_en.name, word_en);
        get(word_en.pos).en.put(word_en.name, word_en);
        hash_en.put(HashString.hash(word_en.name), word_en);
    }

    private void add_ru(final Word word_ru) {
        if (type != TableType.Default && ru.get(word_ru.name) != null) {
            System.out.println("[" + word_ru.name + "] already exist");
        }

        if (Main.ignored_words.stream().anyMatch((" " + word_ru.name + " ")::contains)) {
            System.out.println("[" + word_ru.name + "] was ignored");
            return;
        }

        ru.put(word_ru.name, word_ru);
        get(word_ru.pos).ru.put(word_ru.name, word_ru);
    }

    Set<String> translate_en_ru(final String word_en) {
        var word = en.get(word_en);
        return word != null ? data.get(word.pos).ru.keySet() : Collections.emptySet();
    }

    void loadData(Lang lang) {
        final Scanner sc = getScanner(type + "/" + lang + ".txt");

        for (int i = 0; i < maxID && sc.hasNextLine(); i++) {
            final var line = sc.nextLine().replace('}', ',');
            final var name = line.substring(0, line.indexOf("="))
                    .trim()
                    .replace("['", "")
                    .replace("[\"", "")
                    .replace("']", "")
                    .replace("\"]", "");

            final var pos_name = lang == Lang.ru ? "['поз'] = " : "pos = ";
            requireTrue(line.contains(pos_name));
            final int pos_i = line.indexOf(pos_name) + pos_name.length();
            final int pos = Integer.parseInt(line.substring(pos_i, line.indexOf(',', pos_i)).trim()) - 1;

            final var sec_name = lang == Lang.ru ? "['раздел'] = " : "section = ";
            requireTrue(line.contains(sec_name));
            final int sec_i = line.indexOf(sec_name) + sec_name.length();
            final int section = Integer.parseInt(line.substring(sec_i, line.indexOf(',', sec_i)).trim());

            final boolean outdated = line.contains("['устарел'] = true");

            add(new Word(name, pos, section, outdated), lang);
        }
    }

    private TreeSet<String> parse_dict_id(final String line) {
        final var set = new TreeSet<String>();
        for (int begin = 1, next; begin < line.length(); begin = next + 2) {
            next = line.indexOf(']', begin);
            set.add(line.substring(begin, next).trim());
        }
        return set;
    }

    private void update_dict(final String line, Lang lang, int index) {
        final var node_words = lang == Lang.ru ? get(index).ru : get(index).en;
        final var all_words = lang == Lang.ru ? ru : en;

        for (var name : parse_dict_id(line)) {
            final int idx = name.indexOf('|');
            boolean outdated = false;
            if (idx >= 0) {
                outdated = name.substring(idx + 1).equals("у");
                name = name.substring(0, idx);
            }

            final var original_word = all_words.get(name);
            final var section = original_word != null ? original_word.section
                    : !get(index).en.isEmpty() ? get(index).en.firstEntry().getValue().section : -1;

            final var word = new Word(name, index,  section, outdated);
            node_words.put(name, word);
            all_words.put(name, word);

            if (lang == Lang.en) {
                hash_en.put(HashString.hash(name), word);
            }
        }
    }

    void loadDictionary(final Function<String, String> transform) {
        final Scanner dict = getScanner(type + "/dictionary.txt");

        for (int i = 0; i < maxID && dict.hasNextLine(); i++) {
            final String line = dict.nextLine();
            final int delim = line.indexOf(" = ");
            if (delim < 0) {
                continue;
            }

            update_dict(transform.apply(line.substring(0, delim).trim()), Lang.en, i);
            update_dict(transform.apply(line.substring(delim + 2).trim()), Lang.ru, i);
        }
    }

    void loadDictionary() {
        loadDictionary(e -> e);
    }

    void generateDictionary() {
        final StringBuilder sb = new StringBuilder(maxID * 16);
        for (int i = 0; i < maxID; i++) {
            if (!get(i).en.isEmpty() || !get(i).ru.isEmpty()) {
                get(i).en.keySet().forEach(name -> appendAll(sb, '[', name, ']'));
                sb.append(" = ");
                get(i).ru.forEach((key, value) -> appendAll(sb, '[', key, value.outdated ? "|у]" : "]"));
            }

            sb.append('\n');
        }

        write(type + "/dictionary.txt", sb.toString().stripTrailing());
    }

    void generateOutput(Comparator<Word> comp, boolean use_en_sections) {
        final TreeSet<Word> words = new TreeSet<>(comp);

        if (use_en_sections) {
            for (var word : ru.values()) {
                int section = getFirstElse(get(word.pos).en.values(), word).section;
                words.add(new Word(word.name, word.pos, section, word.outdated));
            }
        } else {
            words.addAll(ru.values());
        }

        final StringBuilder sb = new StringBuilder(maxID * 16);
        for (var word : words) {
            appendAll(sb, "\t\t['", word.name,
                    "'] = { ['поз'] = ", word.pos + 1,
                    ", ['раздел'] = ", word.section,
                    word.outdated ? ", ['устарел'] = true },\n" : " },\n");
        }

        write("output.txt", sb.toString().stripTrailing());
    }

    void generateOutput(Comparator<Word> comp) {
        generateOutput(comp, false);
    }

    void mergeWithByEN(final Table table) {
        for (var entry : table.en.entrySet()) {
            var new_word = entry.getValue();
            var word_en = en.get(new_word.name);
            if (word_en != null) {
                for (var word_ru : table.get(new_word.pos).ru.entrySet()) {
                    final var name_ru = word_ru.getKey();
                    if (!ru.containsKey(name_ru)) {
                        add_ru(new Word(name_ru, word_en.pos, word_en.section, word_ru.getValue().outdated));
                    }
                }
            }
        }
    }

    void mergeWithByPos(final Table table) {
        table.en.values().forEach(this::add_en);
        table.ru.values().forEach(this::add_ru);
    }

    Table transform(Function<String, String> fun_en, Function<String, String> fun_ru) {
        final Table table = new Table(type, maxID);

        for (var word : en.values()) {
            table.add_en(new Word(fun_en.apply(word.name), word.pos, word.section, word.outdated));
        }

        for (var word : ru.values()) {
            table.add_ru(new Word(fun_ru.apply(word.name), word.pos, word.section, word.outdated));
        }

        return table;
    }

    Table transform(Function<String, String> fun) {
        return transform(fun, fun);
    }

    Table upperTypeToLowerType() {
        return transform(Functions::upperTypeToLowerType);
    }

    Table lowerTypeToUpperType() {
        return transform(e -> Functions.lowerTypeToUpperType(e, Lang.en), e -> Functions.lowerTypeToUpperType(e, Lang.ru));
    }

    Table upperTypeToLowerType(Lang lang) {
        return lang == Lang.en
                ? transform(Functions::upperTypeToLowerType, e -> e)
                : transform(e -> e, Functions::upperTypeToLowerType);
    }

    Table lowerTypeToUpperType(Lang lang) {
        return lang == Lang.en
                ? transform(e -> Functions.lowerTypeToUpperType(e, Lang.en), e -> e)
                : transform(e -> e, e -> Functions.lowerTypeToUpperType(e, Lang.ru));
    }

    static String translate_en_ru(final String text, final Table... tables) {
        final int maxLen = 50;
        final var builder = new StringBuilder();
        final var h_str = new HashString(text);

        int last_legal_end_pos = 0;
        final int[] prev_legal_end_pos = new int[text.length() + 1];

        for (int i = 1; i <= text.length(); i++) {
            prev_legal_end_pos[i] = last_legal_end_pos;
            if (legal_pos(text, i)) {
                last_legal_end_pos = i;
            }
        }

        next: for (int begin = 0; begin < text.length(); begin++) {
            if (legal_pos(text, begin)) {
                for (int end = Math.min(text.length(), maxLen + begin); end > begin; end = prev_legal_end_pos[end]) {
                    for (final var table : tables) {
                        final var word_en = table.hash_en.get(h_str.hash(begin, end));
                        if (word_en != null && word_en.name.equals(text.substring(begin, end))) {
                            final var words_ru = table.translate_en_ru(word_en.name);
                            if (!words_ru.isEmpty()) {
                                builder.append(words_ru.iterator().next());
                                begin = end - 1;
                                continue next;
                            }
                        }
                    }
                }
            }

            builder.append(text.charAt(begin));
        }

        return builder.toString();
    }

    private static boolean legal_pos(final String text, final int pos) {
        return pos == 0 || pos == text.length() || !isLetter(text.charAt(pos - 1)) || !isLetter(text.charAt(pos));
    }

    void removeOutdatedWords() {
        ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        for (var node : data) {
            node.ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        }
    }
}
