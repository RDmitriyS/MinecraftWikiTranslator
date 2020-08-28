package main;

import java.util.*;
import java.util.function.Function;

import static main.Functions.*;
import static main.TableType.*;
import static main.Lang.*;

class Main {

    public static void main(String[] args) {
//        generate(InvSprite, 5000);
        translate("input.txt", "output.txt");
//        mergeByLine("text1.txt", "text2.txt");
    }

    static void generate(TableType type, int maxID) {
        var table = new Table(type, maxID);
        table.loadData(en);
        table.loadData(ru);
        table.loadDictionary();
//        table.removeOutdatedWords();
//        table.generateDictionary();
        table.generateOutput(Word::compareByName);
    }

    static void translate(final String input_file_name, final String output_file_name) {
        var tables = new ArrayList<Table>();

        var default_table = getTranslationTable(Default, 2000);
        var default_table_lower = default_table.transform(String::toLowerCase);

        var inv_table = getTranslationTable(InvSprite, 5000);

        var lower_tables = new ArrayList<Table>();
        lower_tables.add(getTranslationTable(ItemSprite, 5000));
        lower_tables.add(getTranslationTable(BlockSprite, 4000));
        lower_tables.add(getTranslationTable(EntitySprite, 2000));
        lower_tables.add(getTranslationTable(BiomeSprite, 400));
        lower_tables.add(getTranslationTable(EffectSprite, 400));
        lower_tables.add(getTranslationTable(EnvSprite, 500));

        tables.add(default_table);
        tables.add(default_table_lower);
        tables.add(inv_table);

        tables.addAll(lower_tables);

        name_transform_and_add(lower_tables, tables, e -> e.replace('-', '_'));
        table_transform_and_add(lower_tables, tables, Table::lowerTypeToUpperType);

        var time1 = System.currentTimeMillis();
        var tables_ = new Table[tables.size()];
        tables.toArray(tables_);
        write(output_file_name, Table.translate_en_ru(readFile(input_file_name), tables_));
        var time2 = System.currentTimeMillis();
        System.out.println((time2 - time1));
    }

    private static Table getTranslationTable(TableType type, int maxID) {
        var table = new Table(type, maxID);
        table.loadDictionary();
        table.removeOutdatedWords();
        return table;
    }

    private static void table_transform_and_add(List<Table> from, List<Table> to, Function<Table, Table> fun) {
        for (Table table : from) {
            to.add(fun.apply(table));
        }
    }

    private static void name_transform_and_add(List<Table> from, List<Table> to, Function<String, String> fun) {
        for (Table table : from) {
            to.add(table.transform(fun));
        }
    }

    // has an effect on the loadData function
    final static Set<String> ignored_words = Set.of(
//            "-бок", "-перед", "-вниз", "-верх", "-низ", "-ножка", "-вертикальные", "-прямые",
//            "-север", "-юг", "-запад", "-восток", "-старая", "-старое", "-старый", "-старые",
//            "pre-", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9",
//            "-вкл", "-выкл",
//            "-be-", "-ce-", "-pe-",
//            " BE", " LCE", " PE",
//            " (версия 1)", " (версия 2)", " (версия 3)",
//            " (предмет)", " блок ", "Блок"
    );
}