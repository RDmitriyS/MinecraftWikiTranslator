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
//        table.loadDictionary();
//        table.removeOutdatedWordsRU();
        table.generateDictionary();
        table.generateOutput(Word::compareByName);
    }

    static void translate(final String input_file_name, final String output_file_name) {
        var tables = new ArrayList<Table>();

        var default_table = getTranslationTable(Default, 5000);
        var default_table_lower = default_table.transform(String::toLowerCase);

        var template_table = getTranslationTable(Template, 500);
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
        tables.add(default_table_lower.transform(e -> e.replace(' ', '-')));
        tables.add(default_table_lower.transform(e -> e.replace(' ', '_')));

        tables.add(template_table);
        tables.add(template_table.transform(String::toLowerCase));

        tables.add(inv_table);

        tables.addAll(lower_tables);

        name_transform_and_add(lower_tables, tables, e -> e.replace('-', '_'));
        name_transform_and_add(lower_tables, tables, e -> e.replace('-', ' '));
        table_transform_and_add(lower_tables, tables, Table::lowerTypeToUpperType);

        var tables_ = new Table[tables.size()];
        tables.toArray(tables_);
        write(output_file_name, Table.translate_en_ru(readFile(input_file_name), tables_));
    }

    private static Table getTranslationTable(TableType type, int maxID) {
        var table = new Table(type, maxID);
        table.loadDictionary();
        table.removeOutdatedWordsRU();
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
}