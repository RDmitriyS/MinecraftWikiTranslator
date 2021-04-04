package main;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.lang.Character.isLetter;
import static java.lang.Character.isLowerCase;
import static main.Exceptions.ILLEGAL_START_END_CHARS;
import static main.Functions.*;

public class Table extends Node {
    static boolean showWarnings = true;

    private final ArrayList<Node> data;
    private final TreeMap<Long, Word> hashEn;

    private final String type;

    Table() {
        this(TableType.Default);
    }

    Table(TableType type) {
        this(type.name());
    }

    Table(String type) {
        this.type = type;
        data = new ArrayList<>();
        hashEn = new TreeMap<>();
    }

    Node get(int index) {
        data.ensureCapacity(index + 1);
        while (data.size() <= index) {
            data.add(null);
        }
        if (data.get(index) == null) {
            data.set(index, new Node());
        }
        return data.get(index);
    }

    int size() {
        return data.size();
    }

    void add(final Word word, Lang lang, final Set<String> ignoredRuWords) {
        if (lang == Lang.ru) {
            addRu(word, ignoredRuWords);
        } else {
            addEn(word);
        }
    }

    void addEn(final Word wordEn) {
        if (showWarnings && en.get(wordEn.name) != null) {
            System.out.println("[" + wordEn.name + "] already exist");
        }

        en.put(wordEn.name, wordEn);
        get(wordEn.pos).en.put(wordEn.name, wordEn);
        hashEn.put(HashString.hash(wordEn.name), wordEn);
    }

    void addRu(final Word wordRu) {
        addRu(wordRu, Collections.emptySet());
    }

    void addRu(final Word wordRu, final Set<String> ignoredRuWords) {
        if (showWarnings && ru.get(wordRu.name) != null) {
            System.out.println("[" + wordRu.name + "] already exist");
        }

        if (ignoredRuWords.stream().anyMatch((" " + wordRu.name + " ")::contains)) {
            if (showWarnings) {
                System.out.println("[" + wordRu.name + "] was ignored");
            }
            return;
        }

        ru.put(wordRu.name, wordRu);
        get(wordRu.pos).ru.put(wordRu.name, wordRu);
    }

    Set<String> translateEnRu(final String wordEn) {
        var word = en.get(wordEn);
        return word != null ? data.get(word.pos).ru.keySet() : Collections.emptySet();
    }

    void loadData(Lang lang) {
        loadData(lang, Collections.emptySet());
    }

    void loadData(Lang lang, final Set<String> ignoredRuWords) {
        final Scanner sc = getScanner(Path.of(type, lang + ".txt"));

        while (sc.hasNextLine()) {
            final var line = sc.nextLine().replace('}', ',');
            final var name = line.substring(0, line.indexOf("="))
                    .trim()
                    .replace("['", "")
                    .replace("[\"", "")
                    .replace("']", "")
                    .replace("\"]", "");

            final var posName = lang == Lang.ru ? "['поз'] = " : "pos = ";
            requireTrue(line.contains(posName));
            final int posIndex = line.indexOf(posName) + posName.length();
            final int pos = Integer.parseInt(line.substring(posIndex, line.indexOf(',', posIndex)).trim()) - 1;

            final var secName = lang == Lang.ru ? "['раздел'] = " : "section = ";
            requireTrue(line.contains(secName));
            final int secIndex = line.indexOf(secName) + secName.length();
            final int section = Integer.parseInt(line.substring(secIndex, line.indexOf(',', secIndex)).trim());

            final boolean outdated = line.contains("['устарел'] = true");

            add(new Word(name, pos, section, outdated), lang, ignoredRuWords);
        }
    }

    private TreeSet<String> parseDictId(final String line) {
        final var set = new TreeSet<String>();
        for (int begin = 1, next; begin < line.length(); begin = next + 2) {
            next = line.indexOf(']', begin);
            set.add(line.substring(begin, next).trim());
        }
        return set;
    }

    private void updateDict(TreeSet<String> names, Lang lang, int pos) {
        final var nodeWords = lang == Lang.ru ? get(pos).ru : get(pos).en;
        final var allWords = lang == Lang.ru ? ru : en;

        for (var name : names) {
            final int idx = name.indexOf('|');
            boolean outdated = false;
            if (idx >= 0) {
                outdated = name.substring(idx + 1).equals("у");
                name = name.substring(0, idx);
            }

            final var originalWord = allWords.get(name);
            final var section = originalWord != null ? originalWord.section
                    : !get(pos).en.isEmpty() ? get(pos).en.firstEntry().getValue().section : -1;

            final var word = new Word(name, pos,  section, outdated);
            nodeWords.put(name, word);
            allWords.put(name, word);

            if (lang == Lang.en) {
                hashEn.put(HashString.hash(name), word);
            }
        }
    }

    void loadDictionary() {
        loadDictionary("dictionary.txt");
    }

    void loadDictionary(String fileName) {
        loadDictionary(fileName, true);
    }

    void loadDictionary(boolean posMatter) {
        loadDictionary("dictionary.txt", posMatter);
    }

    void loadDictionary(String fileName, boolean posMatter) {
        final Scanner dict = getScanner(Path.of(type, fileName));

        for (int i = 0; dict.hasNextLine(); i++) {
            final String line = dict.nextLine();
            final int delim = line.indexOf(" = ");
            if (delim < 0) {
                continue;
            }

            var idEn = parseDictId(line.substring(0, delim).trim());
            var idRu = parseDictId(line.substring(delim + 2).trim());

            int pos = findExisting(idRu, Lang.ru, posMatter);
            if (pos == -1) {
                pos = findExisting(idEn, Lang.en, posMatter);
            }
            if (pos == -1) {
                pos = i;
            }

            updateDict(idEn, Lang.en, pos);
            updateDict(idRu, Lang.ru, pos);
        }
    }

    private int findExisting(TreeSet<String> names, Lang lang, boolean posMatter) {
        final var allWords = lang == Lang.ru ? ru : en;
        int pos = -1;
        for (var name : names) {
            var word = allWords.get(name);
            if (word != null) {
                if (posMatter) {
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
        final StringBuilder sb = new StringBuilder(size() * 16);
        for (Node node : data) {
            if (node == null || node.en.isEmpty() && node.ru.isEmpty()) {
                sb.append('\n');
                continue;
            }

            node.en.keySet().forEach(name -> appendAll(sb, '[', name, ']'));
            sb.append(" = ");
            node.ru.forEach((key, value) -> appendAll(sb, '[', key, value.outdated ? "|у]" : "]"));
            sb.append('\n');
        }

        write(type + "/" + fileName, sb.toString().stripTrailing());
    }

    void generateOutput(Comparator<Word> comp, boolean useEnSections) {
        final TreeSet<Word> words = new TreeSet<>(comp);

        if (useEnSections) {
            for (var word : ru.values()) {
                int section = getFirstElse(get(word.pos).en.values(), word).section;
                words.add(new Word(word.name, word.pos, section, word.outdated));
            }
        } else {
            words.addAll(ru.values());
        }

        final StringBuilder sb = new StringBuilder(size() * 16);
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

    void mergeWithByEn(final Table table) {
        for (var entry : table.en.entrySet()) {
            var newWord = entry.getValue();
            var wordEn = en.get(newWord.name);
            if (wordEn != null) {
                for (var wordRu : table.get(newWord.pos).ru.entrySet()) {
                    final var nameRu = wordRu.getKey();
                    if (!ru.containsKey(nameRu)) {
                        addRu(new Word(nameRu, wordEn.pos, wordEn.section, wordRu.getValue().outdated));
                    }
                }
            }
        }
    }

    void mergeWithByPos(final Table table) {
        table.en.values().forEach(this::addEn);
        table.ru.values().forEach(this::addRu);
    }

    Table transform(Function<String, String> fnEn, Function<String, String> fnRu) {
        final Table table = new Table(type);

        for (var word : en.values()) {
            table.addEn(new Word(fnEn.apply(word.name), word.pos, word.section, word.outdated));
        }

        for (var word : ru.values()) {
            table.addRu(new Word(fnRu.apply(word.name), word.pos, word.section, word.outdated));
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

    static String translateEnRu(String text, Table... tables) {
        final int maxLen = 50;
        final var builder = new StringBuilder();
        final var hashString = new HashString(text);
        final var table = mergeAllByEn(tables);

        int lastLegalEndPos = 0;
        final int[] prevLegalEndPos = new int[text.length() + 1];

        for (int i = 1; i <= text.length(); i++) {
            prevLegalEndPos[i] = lastLegalEndPos;
            if (isLegalPos(text, i)) {
                lastLegalEndPos = i;
            }
        }

        next: for (int begin = 0; begin < text.length(); begin++) {
            boolean beginIsLower = isLowerCase(text.charAt(begin));
            if (!isLegalPos(text, begin) || beginIsLower && begin > 0
                    && ILLEGAL_START_END_CHARS.contains(text.charAt(begin - 1))) {
                builder.append(text.charAt(begin));
                continue;
            }

            for (int end = Math.min(text.length(), maxLen + begin); end > begin; end = prevLegalEndPos[end]) {
                if (beginIsLower && end < text.length() &&
                        ILLEGAL_START_END_CHARS.contains(text.charAt(end))) {
                    continue;
                }

                final var wordEn = table.hashEn.get(hashString.hash(begin, end));
                if (wordEn != null && equal(wordEn.name, text, begin, end)) {
                    final var wordsRu = table.translateEnRu(wordEn.name);
                    if (!wordsRu.isEmpty()) {
                        builder.append(wordsRu.iterator().next());
                        begin = end - 1;
                        continue next;
                    }
                }
            }

            builder.append(text.charAt(begin));
        }

        return builder.toString();
    }

    private static boolean equal(String lhs, String rhs, int rhsBegin, int rhsEnd) {
        if (lhs.length() != rhsEnd - rhsBegin) {
            return false;
        }

        for (int i = 0; i < lhs.length(); i++) {
            if (lhs.charAt(i) != rhs.charAt(i + rhsBegin)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isLegalPos(CharSequence text, int pos) {
        return pos == 0
                || pos == text.length()
                || !isLetter(text.charAt(pos - 1))
                || !isLetter(text.charAt(pos));
    }

    private static Table mergeAllByEn(Table... tables) {
        final boolean showWarnings0 = showWarnings;
        showWarnings = false;

        int pos = 0;
        final var table = new Table();
        for (Table nextTable : tables) {
            for (Node node : nextTable.data) {
                if (node == null || node.en.isEmpty() || node.ru.isEmpty()) {
                    continue;
                }

                boolean added = false;
                for (var name : node.en.keySet()) {
                    if (!table.en.containsKey(name)) {
                        table.addEn(new Word(name, pos));
                        added = true;
                    }
                }

                if (added) {
                    table.addRu(new Word(node.ru.firstKey(), pos++));
                }
            }
        }

        showWarnings = showWarnings0;
        return table;
    }

    void removeOutdatedWordsRu() {
        ru.entrySet().removeIf(entry -> entry.getValue().outdated);
        for (Node node : data) {
            if (node != null) {
                node.ru.entrySet().removeIf(entry -> entry.getValue().outdated);
            }
        }
    }
}
