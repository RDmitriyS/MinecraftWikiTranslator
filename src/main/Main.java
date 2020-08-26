package main;

import java.util.*;

import static main.Functions.*;

class Main {

    public static void main(String[] args) {
        var default_table = new Table(TableType.Default, 1000);
        default_table.loadDictionary();

        var table = new Table(TableType.InvSprite, 5000);
        table.loadData(Lang.en);
        table.loadData(Lang.ru);
        table.loadDictionary();
        table.removeOutdatedWords();
//        table.generateDictionary();
        table.generateOutput(Word::compareByName);

        write("output.txt", Table.translate_en_ru(readFile("text.txt"), default_table, table));
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