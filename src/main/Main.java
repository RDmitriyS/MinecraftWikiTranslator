package main;

import java.util.*;
import java.util.function.Function;

import static main.Functions.*;
import static main.TableType.*;
import static main.Lang.*;

class Main {

    public static void main(String[] args) {
//        generate(ItemSprite);
//        translate("input.txt", "output.txt");
//        mergeByLine("text1.txt", "text2.txt");
    }

    static void generate(TableType type) {
        var table = new Table(type);
        table.loadData(en);
        table.loadData(ru);
//        table.removeOutdatedWordsRu();
        table.generateDictionary();
//        table.generateOutput(Word.nameComparator);
    }

    static void translate(String inputFileName, String outputFileName) {
        Exceptions.ILLEGAL_START_END_CHARS.addAll(List.of('.', '_'));

        var tables = new ArrayList<Table>();

        var defaultTable = getTranslationTable(Default);
        var defaultTableLower = defaultTable.transform(String::toLowerCase);

        var defaultTable2 = getTranslationTable(Default, "dictionary2.txt");

        var templateTable = getTranslationTable(Template);
        var invTable = getTranslationTable(InvSprite);

        var lowerTables = new ArrayList<Table>();
        lowerTables.add(getTranslationTable(ItemSprite));
        lowerTables.add(getTranslationTable(BlockSprite));
        lowerTables.add(getTranslationTable(EntitySprite));
        lowerTables.add(getTranslationTable(BiomeSprite));
        lowerTables.add(getTranslationTable(EffectSprite));
        lowerTables.add(getTranslationTable(EnvSprite));

        tables.add(defaultTable);
        tables.add(defaultTableLower);
//        tables.add(defaultTableLower.transform(e -> e.replace(' ', '-')));
//        tables.add(defaultTableLower.transform(e -> e.replace(' ', '_')));

        tables.add(defaultTable2);

        tables.add(templateTable);
        tables.add(templateTable.transform(String::toLowerCase));

        tables.add(invTable);

//        tables.addAll(lowerTables);
//        nameTransform(lowerTables, tables, e -> e.replace('-', '_'));
//        nameTransform(lowerTables, tables, e -> e.replace('-', ' '));
        tableTransform(lowerTables, tables, Table::lowerTypeToUpperType);

        var tableArray = new Table[tables.size()];
        tables.toArray(tableArray);
        write(outputFileName, Table.translateEnRu(readFile(inputFileName), tableArray));
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
}