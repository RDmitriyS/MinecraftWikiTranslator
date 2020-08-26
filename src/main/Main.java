package main;

import java.util.*;

import static main.Functions.*;

class Main {

    public static void main(String[] args) {
        generate(TableType.InvSprite, 5000);
//        translate("input.txt", "output.txt");
//        mergeByLine("text1.txt", "text2.txt");
    }

    static void generate(TableType type, int maxSize) {
        var table = new Table(type, maxSize);
        table.loadData(Lang.en);
        table.loadData(Lang.ru);
        table.loadDictionary();
//        table.removeOutdatedWords();
//        table.generateDictionary();
        table.generateOutput(Word::compareByName);
    }

    static void translate(final String input_file_name, final String output_file_name) {
        var default_table = new Table(TableType.Default, 5000);
        default_table.loadDictionary();

        var inv_table = new Table(TableType.InvSprite, 5000);
        inv_table.loadDictionary();
        inv_table.removeOutdatedWords();

        var block_table = getTranslationTable(TableType.BlockSprite, 5000);
        var item_table = getTranslationTable(TableType.ItemSprite, 5000);
        var biome_table = getTranslationTable(TableType.BiomeSprite, 1000);
        var effect_table = getTranslationTable(TableType.EffectSprite, 1000);
        var env_table = getTranslationTable(TableType.EnvSprite, 1000);

        write(output_file_name, Table.translate_en_ru(readFile(input_file_name),
                default_table,
                inv_table,
                block_table,
                item_table,
                biome_table,
                effect_table,
                env_table
        ));
    }

    private static Table getTranslationTable(TableType type, int maxSize) {
        var table = new Table(type, maxSize);
        table.loadDictionary();
        table.removeOutdatedWords();
        return table.lowerTypeToUpperType();
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