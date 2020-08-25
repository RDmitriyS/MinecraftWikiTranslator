package main;

import java.util.*;
import java.util.function.Function;
import static main.Functions.*;

class Main {

    public static void main(String[] args) {
        var default_table = new Table(TableType.Default, 500);
        default_table.loadDictionary();

        var table = new Table(TableType.InvSprite, 4000);
        table.loadData(Lang.ru);
        table.loadData(Lang.en);
//        table.generateDictionary();
        table.loadDictionary();
//        table.generateOutput(Word::compareByName);

        write("output.txt", Table.translate_en_ru(readFile("text.txt"), default_table, table));
    }

    final static Set<String> ignored_words = Set.of(
//            "-бок", "-перед", "-вниз", "-верх", "-низ", "-ножка", "-вертикальные", "-прямые",
//            "-север", "-юг", "-запад", "-восток", "-старая", "-старое", "-старый", "pre-",
//            "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-вкл", "-выкл",
//            "-be-", "-ce-", "-pe-",
//            " BE", " LCE", " PE", " (версия 1)", " (версия 2)", " (версия 3)"
    );

    static class Word implements Comparable<Word> {
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
            return name.compareTo(word.name);
        }

        public int compareByName(final Word word) {
            final int temp = name.compareTo(word.name);
            return temp != 0 ? temp : Integer.compare(pos, word.pos);
        }

        public int compareByPos(final Word word) {
            final int temp = Integer.compare(pos, word.pos);
            return temp != 0 ? temp : name.compareTo(word.name);
        }
    }

    static private class Node {
        protected final TreeMap<String, Word> en, ru;
        protected final TreeMap<Long, Word> hash_en;

        Node() {
            en = new TreeMap<>();
            ru = new TreeMap<>();
            hash_en = new TreeMap<>();
        }
    }

    static class Table extends Node {
        private final List<Node> data;

        private final TableType type;
        private final int maxSize;

        Table(TableType type, int maxSize) {
            this.type = type;
            this.maxSize = maxSize;
            data = new ArrayList<>(maxSize);
            for (int i = 0; i < maxSize; i++) {
                data.add(new Node());
            }
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

            if (ignored_words.stream().anyMatch((word_ru.name + " ")::contains)) {
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
                final int pos_i = line.indexOf(pos_name) + pos_name.length();
                final int pos = Integer.parseInt(line.substring(pos_i, line.indexOf(',', pos_i)).trim()) - 1;

                final var sec_name = lang == Lang.ru ? "['раздел'] = " : "section = ";
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

            final var names = parse_dict_id(line);
            node_words.keySet().removeIf(name -> {
                final int idx = name.indexOf('|');
                return !names.contains(idx < 0 ? name : name.substring(0, idx));
            });

            for (var name : names) {
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
                    continue;
                }

                final var words_en = get(i).en;
                final var words_ru = get(i).ru;

                int section = !words_en.isEmpty() ? words_en.firstEntry().getValue().section
                        : !words_ru.isEmpty() ? words_ru.firstEntry().getValue().section : -1;

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

        void generateOutput(Comparator<Word> comp) {
            final TreeSet<Word> words = new TreeSet<>(comp);

            for (Node node : data) {
                words.addAll(node.ru.values());
            }

            final StringBuilder sb = new StringBuilder(maxSize * 16);
            for (var word : words) {
                appendAll(sb, "\t\t['", word.name, "'] = { ['поз'] = ", word.pos + 1, ", ['раздел'] = ", word.section,
                        word.outdated ? ", ['устарел'] = true },\n" : " },\n");
            }

            write("output.txt", sb.toString().stripTrailing());
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
                            add_ru(new Word(name_ru, word_en.pos, word_en.section));
                        }
                    }
                }
            }
        }

        Table transform(Function<String, String> fun_en, Function<String, String> fun_ru) {
            final Table table = new Table(type, maxSize);

            for (var word : en.values()) {
                table.add_en(new Word(fun_en.apply(word.name), word.pos, word.section));
            }

            for (var word : ru.values()) {
                table.add_ru(new Word(fun_ru.apply(word.name), word.pos, word.section));
            }

            return table;
        }

        Table toLowerCase() {
            return transform(Main::toLowerCase, Main::toLowerCase);
        }

        Table toUpperCase() {
            return transform(e -> Main.toUpperCase(e, Lang.en), e -> Main.toUpperCase(e, Lang.ru));
        }


        Table toLowerCase(Lang lang) {
            return lang == Lang.en
                    ? transform(Main::toLowerCase, e -> e)
                    : transform(e -> e, Main::toLowerCase);
        }

        Table toUpperCase(Lang lang) {
            return lang == Lang.en
                    ? transform(e -> Main.toUpperCase(e, Lang.en), e -> e)
                    : transform(e -> e, e -> Main.toUpperCase(e, Lang.ru));
        }

        static String translate_en_ru(final String text, final Table... tables) {
            final int maxLen = 50;
            final var builder = new StringBuilder();
            final var h_str = new HashString(text);

            for (int i = 0; i < text.length(); i++) {
                boolean found = false;

                if (Character.isAlphabetic(text.charAt(i))) {
                    for (int j = Math.min(text.length(), maxLen + i); j > i; j--) {
                        for (final var table : tables) {
                            final var word_en = table.hash_en.get(h_str.hash(i, j));
                            if (word_en != null && word_en.name.equals(text.substring(i, j))) {
                                final var words_ru = table.translate_en_ru(word_en.name);
                                if (!words_ru.isEmpty()) {
                                    builder.append(words_ru.iterator().next());
                                    i = j - 1;
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!found) {
                    builder.append(text.charAt(i));
                }
            }

            return builder.toString();
        }
    }

    static String toUpperCase(String val, Lang lang) {
        var temp = name_exceptions_lower.get(val);
        if (temp != null) {
            return temp;
        }

        final var sb = new StringBuilder(val);

        if (lang == Lang.ru) {
            for (var key : dash_exceptions) {
                final int index = sb.indexOf(key);
                if (index >= 0) {
                    sb.setCharAt(sb.indexOf("-", index), '+');
                }
            }
        }

        for (int end = 0, begin = 0; end <= sb.length(); end++) {
            if (end == sb.length() || sb.charAt(end) == '-') {
                final var new_word = word_exceptions_lower.get(sb.substring(begin, end));
                if (new_word != null) {
                    sb.replace(begin, end, new_word);
                } else if (lang == Lang.en || begin == 0) {
                    sb.setCharAt(begin, Character.toUpperCase(sb.charAt(begin)));
                }
                begin = end + 1;
            }
        }

        return sb.toString().replace('-', ' ').replace('+', '-');
    }

    static String toLowerCase(String val) {
        return val.toLowerCase().replace(' ', '-');
    }

    final static Set<String> word_exceptions = Set.of(
            "a", "the", "on", "and", "with", "o'", "of",
            "BE", "LCE", "PE", "CE", "Edition", "Bedrock", "Education",
            "Края", "Энд", "Энда", "Эндера", "Нижнего мира", "Незера",
            "ТНТ", "чем-то"
    );

    final static Set<String> name_exceptions = Set.of(
            "USB-блок",
            "Блок-конструктор",
            "Ведро с рыбой-клоуном",
            "Лук-батун",
            "Блок-пазл",
            "Рыба-клоун",
            "Какао-бобы",
            "Светильник Джека",
            "Jack o'Lantern",
            "Alkali metal",
            "Alkaline earth metal",
            "Noble gas",
            "Other non-metal",
            "Post transition metal",
            "Transition metal"
    );

    final static Set<String> dash_exceptions = Set.of(
            "светло-", "pre-", "визер-", "верхне-", "нижне-",
            "эндер-", "тёмно-", "сундук-ловушк", "кожано-", "незер-",
            "зомби-жител", "зомби-крестьянин", "зомби-свиночеловек",
            "лошади-зомби", "лошади-скелет", "рыбы-клоун", "скелета-иссушител"
    );

    final static Map<String, String> word_exceptions_lower = to_lower_upper_map(word_exceptions);
    final static Map<String, String> name_exceptions_lower = to_lower_upper_map(name_exceptions);

    static void mergeByLine(String path1, String path2) {
        var reader1 = getScanner(path1);
        var reader2 = getScanner(path2);
        while (reader1.hasNextLine() && reader2.hasNextLine()) {
            System.out.println((reader1.nextLine() + reader2.nextLine()));
        }
    }
}