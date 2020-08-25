package main;

import java.util.*;
import static main.Functions.*;
import static main.Exceptions.*;

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
}