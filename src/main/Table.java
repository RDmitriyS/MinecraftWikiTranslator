package main;

import java.util.*;
import java.util.function.Function;

import static java.lang.Character.isLetter;
import static java.lang.Character.isLowerCase;
import static main.Exceptions.illegal_start_end_chars;
import static main.Functions.*;

public class Table extends Node {
    static boolean showWarnings = true;

    private final List<Node> data;
    private final TreeMap<Long, Word> hash_en;

    private final String type;
    private final int maxID;

    Table(TableType type, int maxID) {
        this(type.name(), maxID);
    }

    Table(String type, int maxID) {
        this.type = type;
        this.maxID = maxID;
        data = new ArrayList<>(maxID);
        for (int i = 0; i < maxID; i++) {
            data.add(new Node());
        }

        hash_en = new TreeMap<>();
    }

    Node get(int index) {
        return data.get(index);
    }

    void add(final Word word, Lang lang, final Set<String> ignored_ru_words) {
        if (lang == Lang.ru) {
            add_ru(word, ignored_ru_words);
        } else {
            add_en(word);
        }
    }

    void add_en(final Word word_en) {
        if (showWarnings && en.get(word_en.name) != null) {
            System.out.println("[" + word_en.name + "] already exist");
        }

        en.put(word_en.name, word_en);
        get(word_en.pos).en.put(word_en.name, word_en);
        hash_en.put(HashString.hash(word_en.name), word_en);
    }

    void add_ru(final Word word_ru) {
        add_ru(word_ru, Collections.emptySet());
    }

    void add_ru(final Word word_ru, final Set<String> ignored_ru_words) {
        if (showWarnings && ru.get(word_ru.name) != null) {
            System.out.println("[" + word_ru.name + "] already exist");
        }

        if (ignored_ru_words.stream().anyMatch((" " + word_ru.name + " ")::contains)) {
            if (showWarnings) {
                System.out.println("[" + word_ru.name + "] was ignored");
            }
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
        loadData(lang, Collections.emptySet());
    }

    void loadData(Lang lang, final Set<String> ignored_ru_words) {
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

            add(new Word(name, pos, section, outdated), lang, ignored_ru_words);
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

    private void update_dict(TreeSet<String> names, Lang lang, int pos) {
        final var node_words = lang == Lang.ru ? get(pos).ru : get(pos).en;
        final var all_words = lang == Lang.ru ? ru : en;

        for (var name : names) {
            final int idx = name.indexOf('|');
            boolean outdated = false;
            if (idx >= 0) {
                outdated = name.substring(idx + 1).equals("у");
                name = name.substring(0, idx);
            }

            final var original_word = all_words.get(name);
            final var section = original_word != null ? original_word.section
                    : !get(pos).en.isEmpty() ? get(pos).en.firstEntry().getValue().section : -1;

            final var word = new Word(name, pos,  section, outdated);
            node_words.put(name, word);
            all_words.put(name, word);

            if (lang == Lang.en) {
                hash_en.put(HashString.hash(name), word);
            }
        }
    }

    void loadDictionary() {
        loadDictionary("dictionary.txt");
    }

    void loadDictionary(String fileName) {
        loadDictionary(fileName, true);
    }

    void loadDictionary(boolean pos_matter) {
        loadDictionary("dictionary.txt", false);
    }

    void loadDictionary(String fileName, boolean pos_matter) {
        final Scanner dict = getScanner(type + "/" + fileName);

        for (int i = 0; i < maxID && dict.hasNextLine(); i++) {
            final String line = dict.nextLine();
            final int delim = line.indexOf(" = ");
            if (delim < 0) {
                continue;
            }

            var id_en = parse_dict_id(line.substring(0, delim).trim());
            var id_ru = parse_dict_id(line.substring(delim + 2).trim());

            int pos = findExisting(id_ru, Lang.ru, pos_matter);
            if (pos == -1) {
                pos = findExisting(id_en, Lang.en, pos_matter);
            }
            if (pos == -1) {
                pos = i;
            }

            update_dict(id_en, Lang.en, pos);
            update_dict(id_ru, Lang.ru, pos);
        }
    }

    private int findExisting(TreeSet<String> names, Lang lang, boolean pos_matter) {
        final var all_words = lang == Lang.ru ? ru : en;
        int pos = -1;
        for (var name : names) {
            var word = all_words.get(name);
            if (word != null) {
                if (pos_matter) {
                    if (showWarnings) {
                        System.out.println("[" + word.name + "] already exist");
                    }
                } else {
                    pos = word.pos;
                    break;
                }
            }
        }
        return pos;
    }

    void generateDictionary() {
        generateDictionary("dictionary.txt");
    }

    void generateDictionary(String fileName) {
        final StringBuilder sb = new StringBuilder(maxID * 16);
        for (int i = 0; i < maxID; i++) {
            if (!get(i).en.isEmpty() || !get(i).ru.isEmpty()) {
                get(i).en.keySet().forEach(name -> appendAll(sb, '[', name, ']'));
                sb.append(" = ");
                get(i).ru.forEach((key, value) -> appendAll(sb, '[', key, value.outdated ? "|у]" : "]"));
            }

            sb.append('\n');
        }

        write(type + "/" + fileName, sb.toString().stripTrailing());
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

        write("table.txt", sb.toString().stripTrailing());
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
        final var table = mergeAllByEN(tables);

        int last_legal_end_pos = 0;
        final int[] prev_legal_end_pos = new int[text.length() + 1];

        for (int i = 1; i <= text.length(); i++) {
            prev_legal_end_pos[i] = last_legal_end_pos;
            if (legal_pos(text, i)) {
                last_legal_end_pos = i;
            }
        }

        next: for (int begin = 0; begin < text.length(); begin++) {
            boolean beginIsLower = isLowerCase(text.charAt(begin));
            if (!legal_pos(text, begin) || beginIsLower && begin > 0
                    && illegal_start_end_chars.contains(text.charAt(begin - 1))) {
                builder.append(text.charAt(begin));
                continue;
            }

            for (int end = Math.min(text.length(), maxLen + begin); end > begin; end = prev_legal_end_pos[end]) {
                if (beginIsLower && end < text.length() &&
                        illegal_start_end_chars.contains(text.charAt(end))) {
                    continue;
                }

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

            builder.append(text.charAt(begin));
        }

        return builder.toString();
    }

    private static boolean legal_pos(final String text, final int pos) {
        return pos == 0 || pos == text.length() || !isLetter(text.charAt(pos - 1)) || !isLetter(text.charAt(pos));
    }

    private static Table mergeAllByEN(final Table... tables) {
        final boolean showWarnings0 = showWarnings;
        showWarnings = false;
        int maxID = 0;
        for (Table table : tables) {
            maxID += table.maxID;
        }

        int pos = 0;
        final var table = new Table(TableType.Default, maxID);
        for (Table next_table : tables) {
            for (Node node : next_table.data) {
                if (node.en.isEmpty() || node.ru.isEmpty()) {
                    continue;
                }

                boolean added = false;
                for (var name : node.en.keySet()) {
                    if (!table.en.containsKey(name)) {
                        table.add_en(new Word(name, pos));
                        added = true;
                    }
                }

                if (added) {
                    table.add_ru(new Word(node.ru.firstKey(), pos++));
                }
            }
        }

        showWarnings = showWarnings0;
        return table;
    }

    void removeOutdatedWordsRU() {
        ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        for (var node : data) {
            node.ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        }
    }
}
