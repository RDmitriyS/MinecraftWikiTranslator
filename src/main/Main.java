package main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;

import static main.Functions.*;
import static main.Table.showWarnings;
import static main.TableType.*;
import static main.Lang.*;

class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
//        generateDictionary(BlockSprite);
//        generateTable(BlockSprite);
        translate();
//        mergeByLine("text1.txt", "text2.txt");
//        updateAllContent();
//        generateAllDictionaries();
//        generateAllTables();
    }

    static void translate(String inputFileName, String outputFileName) {
        var settings = new Settings();
        Exceptions.ILLEGAL_START_END_CHARS.addAll(List.of('.', '_'));

        var tables = new ArrayList<Table>();
        var defaultTable = getTranslationTable(Default);
        var defaultTable2 = getTranslationTable(Default, "dictionary2.txt");
        var invTable = getTranslationTable(InvSprite);
        tables.add(defaultTable);
        tables.add(defaultTable2.lowerTypeToUpperType());
        tables.add(invTable);

        var lowerTables = new ArrayList<Table>();
        lowerTables.add(getTranslationTable(ItemSprite));
        lowerTables.add(getTranslationTable(BlockSprite));
        lowerTables.add(getTranslationTable(EntitySprite));
        lowerTables.add(getTranslationTable(BiomeSprite));
        lowerTables.add(getTranslationTable(EffectSprite));
        lowerTables.add(getTranslationTable(EnvSprite));
        tableTransform(lowerTables, tables, Table::lowerTypeToUpperType);

        if (settings.translateLowercaseWords) {
            var defaultTableLower = defaultTable.transform(String::toLowerCase);
            var templateTable = getTranslationTable(Template);

            tables.add(defaultTableLower);
//            tables.add(defaultTableLower.transform(e -> e.replace(' ', '-')));
//            tables.add(defaultTableLower.transform(e -> e.replace(' ', '_')));

            tables.add(defaultTable2);
            tables.add(templateTable);
            tables.add(templateTable.transform(String::toLowerCase));

//            tables.addAll(lowerTables);
//            nameTransform(lowerTables, tables, e -> e.replace('-', '_'));
//            nameTransform(lowerTables, tables, e -> e.replace('-', ' '));
        }

        var tableArray = new Table[tables.size()];
        tables.toArray(tableArray);
        var input = inputFileName == null ? "input.txt" : inputFileName;
        var output = outputFileName == null ? "output.txt" : outputFileName;
        write(output, Table.translateEnRu(readFile(input), tableArray));
    }

    static void translate() {
        translate("input.txt", "output.txt");
    }

    private static Table getTranslationTable(TableType type) {
        return getTranslationTable(type, "dictionary.txt");
    }

    private static Table getTranslationTable(TableType type, String dictName) {
        var table = new Table(type);
        table.loadDictionary(dictName);
        table.removeOutdatedWordsRu();
        return table;
    }

    private static void tableTransform(List<Table> fromList, List<Table> toList, Function<Table, Table> fn) {
        fromList.parallelStream().map(fn).forEachOrdered(toList::add);
    }

    private static void nameTransform(List<Table> fromList, List<Table> toList, Function<String, String> fn) {
        tableTransform(fromList, toList, table -> table.transform(fn));
    }

    static void updateContent(Lang lang, TableType type) {
        var wikiTag = lang == ru ? "ru" : "";
        var module = "Module:" + type.getModuleName(lang);
        var url = "https://minecraft.fandom.com/" + wikiTag +
                "/api.php" +
                "?action=query" +
                "&prop=revisions" +
                "&titles=" + module +
                "&rvslots=*" +
                "&rvprop=content" +
                "&formatversion=2" +
                "&format=json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        String text = response.body();
        String begin = "\"content\":\"";
        int beginIndex = text.indexOf(begin) + begin.length();
        int endIndex = text.lastIndexOf('"');
        text = text
            .substring(beginIndex, endIndex)
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("    ", "\t");

        Functions.write(type.name() + "/" + lang.name() + ".txt", text);
    }

    static void updateAllContent(Lang lang) {
        Arrays.stream(TableType.values())
                .filter(TableType::isModule)
                .parallel()
                .forEach(type -> updateContent(lang, type));
    }

    static void updateAllContent() {
        updateAllContent(ru);
        updateAllContent(en);
    }

    static void generateDictionary(TableType type) {
        generateDictionary(type.name());
    }

    static void generateDictionary(String name) {
        var table = new Table(name);
        table.loadData(en);
        table.loadData(ru);
        table.generateDictionary();
    }

    static void generateAllDictionaries() {
        Arrays.stream(TableType.values())
            .filter(TableType::isModule)
            .parallel()
            .forEach(type -> generateDictionary(type.name()));
    }

    static void generateTable(TableType type) {
        generateTable(type.name(), null);
    }

    static void generateTable(String name, Comparator<Word> comp) {
        var table = new Table(name);
        table.loadData(en);
        table.loadData(ru);
        showWarnings = false;
        table.loadDictionary();
        if (comp == null) {
            if (name.equals("BlockSprite")) {
                comp = Word.posComparator;
            } else {
                comp = Word.nameComparator;
            }
        }
        table.generateTable(comp);
    }

    static void generateAllTables() {
        Arrays.stream(TableType.values())
            .filter(TableType::isModule)
            .parallel()
            .forEach(type -> generateTable(type.name(), null));
    }
}