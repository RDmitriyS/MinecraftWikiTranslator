package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import static main.Lang.en;
import static main.Lang.ru;
import static main.TableType.Default;
import static main.TableType.InvSprite;

public class SomeFunctions {

    public static void main(String[] args) {

    }

    static void uniqueness(TableType type, int maxID) {
        var def_table = new Table(Default, 5000);
        def_table.loadDictionary();

        var table = new Table(type, maxID);
        table.loadDictionary();

        var rd = new Scanner(System.in);

        PrintWriter wr;
        try {
            wr = new PrintWriter("input.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        var table2 = new Table(type, maxID);
        int pos = 0;
        for (int i = 0; i < maxID; i++) {
            var node = table.get(i);
            if (node.en.size() > 1 || node.ru.size() > 1) {
                for (var word_en : node.en.values()) {
                    if (def_table.en.containsKey(word_en.name) || !word_en.name.contains("Spawn")) {
                        continue;
                    }

                    System.out.println(word_en.name);

                    var array = new Word[node.ru.size()];
                    node.ru.values().toArray(array);

                    for (int j = 0; j < node.ru.size(); j++) {
                        System.out.println(j + 1 + " : " + array[j].name);
                    }

                    var out = "";
                    while ((out = rd.nextLine()).isEmpty());
                    wr.println(out);
                    wr.flush();

                    if (out.length() > 2) {
                        var word_ru = table2.ru.get(out);
                        if (word_ru == null) {
                            table2.add_en(new Word(word_en.name, pos));
                            table2.add_ru(new Word(out, pos++));
                        } else {
                            table2.add_en(new Word(word_en.name, word_ru.pos));
                        }
                    } else {
                        int t = Integer.parseInt(out);
                        if (t != 0) {
                            var word_ru = table2.ru.get(array[t - 1].name);
                            if (word_ru == null) {
                                table2.add_en(new Word(word_en.name, pos));
                                table2.add_ru(new Word(array[t - 1].name, pos++));
                            } else {
                                table2.add_en(new Word(word_en.name, word_ru.pos));
                            }
                        }
                    }
                }
            } /*else if (!node.ru.isEmpty()) {
                var word_ru = table2.ru.get(node.ru.firstEntry().getValue().name);
                if (word_ru == null) {
                    final var pp = pos;
                    node.en.values().forEach(word -> table2.add_en(new Word(word.name, pp)));
                    table2.add_ru(new Word(node.ru.firstEntry().getValue().name, pos++));
                } else {
                    node.en.values().forEach(word -> table2.add_en(new Word(word.name, word_ru.pos)));
                }
            }*/
        }

        table2.generateDictionary("dict.txt");
    }

    static void merge() {
        var table = new Table(Default, 5000);
        table.loadDictionary();
        var table2 = new Table(InvSprite, 5000);
        table2.loadDictionary("dict.txt");

        for (int i = 0; i < 5000; i++) {
            var entry  = table2.get(i);

            if (!entry.en.isEmpty()) {
                var en = entry.en.firstEntry().getValue();
                var ru = entry.ru.firstEntry().getValue();
                if (table.ru.containsKey(ru.name)) {
                    table.add_en(new Word(en.name, table.ru.get(ru.name).pos));
                } else if (!table.en.containsKey(en.name)) {
                    table.add_ru(ru);
                    table.add_en(en);
                }
            }
        }

        table.generateDictionary();
    }

    static void update_table() {
        var table = new Table(Default, 5000);
        table.loadDictionary();

        var table2 = new Table(InvSprite, 5000);
        table2.loadData(ru);
        table2.loadData(en);
        table2.loadDictionary();

        var table3 = new Table(Default, 5000);

        table2.en.values().forEach(table3::add_en);

        for (var entry : table2.ru.entrySet()) {
            if (!table.ru.containsKey(entry.getKey())) {
                var word = entry.getValue();
                table3.add_ru(new Word(word.name, word.pos, word.section, true));
            }
        }

        for (int i = 0; i < 5000; i++) {
            var entry = table.get(i);
            if (entry.en.isEmpty()) {
                continue;
            }

            Word word = null;

            for (var name : entry.en.keySet()) {
                if ((word = table2.en.get(name)) != null) {
                    break;
                }
            }

            if (word == null) {
                for (var name : entry.ru.keySet()) {
                    if ((word = table2.ru.get(name)) != null) {
                        break;
                    }
                }
            }

            if (word != null) {
                for (var name : entry.ru.keySet()) {
                    table3.add_ru(new Word(name, word.pos, word.section));
                }
            }
        }

        for (int i = 0; i < 5000; i++) {
            var entry = table3.get(i);
            boolean out = true;
            for (var word : entry.ru.values()) {
                out = out && word.outdated;
            }

            if (out) {
                for (var word : entry.ru.values()) {
                    table3.add_ru(new Word(word.name, word.pos, word.section));
                }
            }
        }


        table3.generateOutput(Word::compareByName);
    }
}
