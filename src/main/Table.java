package main;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Character.isLetter;
import static java.lang.Character.isAlphabetic;
import static main.Functions.*;

public class Table extends Node {
    private final List<Node> data;
    private final TreeMap<Long, Word> hash_en;

    private final TableType type;
    private final int maxSize;

    Table(TableType type, int maxSize) {
        this.type = type;
        this.maxSize = maxSize;
        data = new ArrayList<>(maxSize);
        for (int i = 0; i < maxSize; i++) {
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

        for (int i = 0; i < maxSize && sc.hasNextLine(); i++) {
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

    private void update_dict(final String line, Lang lang, int index, int section) {
        final var node_words = lang == Lang.ru ? get(index).ru : get(index).en;
        final var all_words = lang == Lang.ru ? ru : en;

        for (var name : parse_dict_id(line)) {
            final int idx = name.indexOf('|');
            boolean outdated = false;
            if (idx >= 0) {
                outdated = name.substring(idx + 1).equals("у");
                name = name.substring(0, idx);
            }

            final var word = new Word(name, index, section, outdated);
            node_words.put(name, word);
            all_words.put(name, word);

            if (lang == Lang.en) {
                hash_en.put(HashString.hash(name), word);
            }
        }
    }

    void loadDictionary(final Function<String, String> transform) {
        final Scanner dict = getScanner(type + "/dictionary.txt");

        for (int i = 0; i < maxSize && dict.hasNextLine(); i++) {
            final String line = dict.nextLine();
            final int delim = line.indexOf(" = ");
            if (delim < 0) {
                get(i).en.clear();
                get(i).ru.clear();
                continue;
            }

            final var words_en = get(i).en;
            final var words_ru = get(i).ru;

            int section = !words_ru.isEmpty() ? words_ru.firstEntry().getValue().section
                        : !words_en.isEmpty() ? words_en.firstEntry().getValue().section : -1;

            update_dict(transform.apply(line.substring(0, delim).trim()), Lang.en, i, section);
            update_dict(transform.apply(line.substring(delim + 2).trim()), Lang.ru, i, section);
        }
    }

    void loadDictionary() {
        loadDictionary(e -> e);
    }

    void generateDictionary() {
        final StringBuilder sb = new StringBuilder(maxSize * 16);
        for (int i = 0; i < maxSize; i++) {
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
            words.addAll(ru.values().stream().map(word ->
                    new Word(word.name, word.pos, getFirstElse(get(word.pos).en.values(), word).section)).collect(Collectors.toSet()));
        } else {
            words.addAll(ru.values());
        }

        final StringBuilder sb = new StringBuilder(maxSize * 16);
        for (var word : words) {
            appendAll(sb, "\t\t['", word.name, "'] = { ['поз'] = ", word.pos + 1, ", ['раздел'] = ", word.section,
                    word.outdated ? ", ['устарел'] = true },\n" : " },\n");
        }

        write("output.txt", sb.toString().stripTrailing());
    }

    void generateOutput(Comparator<Word> comp) {
        generateOutput(comp, false);
    }

    // merge this_table with table by en words
    void mergeWith(final Table table) {
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

    Table transform(Function<String, String> fun_en, Function<String, String> fun_ru) {
        final Table table = new Table(type, maxSize);

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

    Table toLowerCase() {
        return transform(Functions::toLowerCase);
    }

    Table toUpperCase() {
        return transform(e -> Functions.toUpperCase(e, Lang.en), e -> Functions.toUpperCase(e, Lang.ru));
    }

    Table toRuUpperCase() {
        return transform(e -> Functions.toUpperCase(e, Lang.ru));
    }

    Table toLowerCase(Lang lang) {
        return lang == Lang.en
                ? transform(Functions::toLowerCase, e -> e)
                : transform(e -> e, Functions::toLowerCase);
    }

    Table toUpperCase(Lang lang) {
        return lang == Lang.en
                ? transform(e -> Functions.toUpperCase(e, Lang.en), e -> e)
                : transform(e -> e, e -> Functions.toUpperCase(e, Lang.ru));
    }

    static String translate_en_ru(final String text, final Table... tables) {
        final int maxLen = 50;
        final var builder = new StringBuilder();
        final var h_str = new HashString(text);

        next: for (int begin = 0; begin < text.length(); begin++) {
            if (isAlphabetic(text.charAt(begin)) && (begin == 0 || !isLetter(text.charAt(begin - 1)))) {
                for (int end = Math.min(text.length(), maxLen + begin); end > begin; end--) {
                    if (end != text.length() && isLetter(text.charAt(end))) {
                        continue;
                    }

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

    void removeOutdatedWords() {
        ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        for (var node : data) {
            node.ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        }
    }
}
