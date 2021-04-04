package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import static main.Lang.en;
import static main.Lang.ru;
import static main.TableType.*;

public class TestClass {

    public static void main(String[] args) {
//        uniqueness(BlockSprite, 5000);
//        merge();
//        updateTable();
        Functions.write("output.txt", work(Functions.readFile("input.txt")));
    }

    static void uniqueness(TableType type) {
        var defTable = new Table();
        defTable.loadDictionary();
        defTable = defTable.transform(Functions::upperTypeToLowerType);

        var defTable2 = new Table();
        defTable2.loadDictionary("dictionary2.txt");

        var table = new Table(type);
        table.loadDictionary();

        var rd = new Scanner(System.in);

        PrintWriter wr;
        try {
            wr = new PrintWriter("input.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }


        HashMap<String, String> map = new HashMap<>();
        map.put("-pre-texture-update", "-до-texture-update");
        map.put("-up", "-вверх");
        map.put("-down", "-вниз");
        map.put("-left", "-влево");
        map.put("-right", "-вправо");
        map.put("-top", "-верх");
        map.put("-bottom", "-низ");
        map.put("-side", "-бок");
        map.put("-north", "-север");
        map.put("-south", "-юг");

        var table2 = new Table(type);
        int pos = 0;
        int size = table2.size();

        for (int i = 0; i < size; i++) {
            var node = table.get(i);
            if (node.en.size() > 1 || node.ru.size() > 1) {
                next: for (var wordEn : node.en.values()) {

                    if (defTable2.en.containsKey(wordEn.name)) {
                        continue;
                    }

                    if (defTable.en.containsKey(wordEn.name)) {
                        var nameRu = defTable.translateEnRu(wordEn.name).iterator().next();
                        if (!nameRu.contains("(")) {
                            var wordRu = table2.ru.get(nameRu);
                            if (wordRu == null) {
                                table2.addEn(new Word(wordEn.name, pos));
                                table2.addRu(new Word(nameRu, pos++));
                            } else {
                                table2.addEn(new Word(wordEn.name, wordRu.pos));
                            }
                        }

                        continue;
                    }

                    for (var part : map.entrySet()) {
                        if (wordEn.name.endsWith(part.getKey())) {
                            var nameEn = wordEn.name.substring(0, wordEn.name.length() - part.getKey().length());
                            var namesRu = defTable2.translateEnRu(nameEn);
                            if (namesRu.isEmpty()) {
                                namesRu = defTable.translateEnRu(nameEn);
                            }
                            if (!namesRu.isEmpty()) {
                                var nameRu = namesRu.iterator().next() + part.getValue();
                                var wordRu = table2.ru.get(nameRu);
                                if (wordRu == null) {
                                    table2.addEn(new Word(wordEn.name, pos));
                                    table2.addRu(new Word(nameRu, pos++));
                                } else {
                                    table2.addEn(new Word(wordEn.name, wordRu.pos));
                                }

                                continue next;
                            }
                        }
                    }

                    if (wordEn.name.contains("-wall")) {
                        continue;
                    }

                    System.out.println(wordEn.name);

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
                        var wordRu = table2.ru.get(out);
                        if (wordRu == null) {
                            table2.addEn(new Word(wordEn.name, pos));
                            table2.addRu(new Word(out, pos++));
                        } else {
                            table2.addEn(new Word(wordEn.name, wordRu.pos));
                        }
                    } else {
                        int t = Integer.parseInt(out);
                        if (t != 0) {
                            var wordRu = table2.ru.get(array[t - 1].name);
                            if (wordRu == null) {
                                table2.addEn(new Word(wordEn.name, pos));
                                table2.addRu(new Word(array[t - 1].name, pos++));
                            } else {
                                table2.addEn(new Word(wordEn.name, wordRu.pos));
                            }
                        }
                    }
                }
            }/* else if (!node.ru.isEmpty()) {
                var wordRu = table2.ru.get(node.ru.firstEntry().getValue().name);
                if (wordRu == null) {
                    final var pp = pos;
                    node.en.values().forEach(word -> table2.addEn(new Word(word.name, pp)));
                    table2.addRu(new Word(node.ru.firstEntry().getValue().name, pos++));
                } else {
                    node.en.values().forEach(word -> table2.addEn(new Word(word.name, wordRu.pos)));
                }
            }*/
        }

        table2.generateDictionary("dict.txt");
    }

    static void merge() {
        var table = new Table();
        table.loadDictionary("dictionary2.txt");
        var table2 = new Table(BlockSprite);
        table2.loadDictionary("dict.txt");

        for (int i = 0; i < 5000; i++) {
            var entry = table2.get(i);

            if (!entry.en.isEmpty()) {
                var en = entry.en.firstEntry().getValue();
                var ru = entry.ru.firstEntry().getValue();
                if (table.ru.containsKey(ru.name)) {
                    table.addEn(new Word(en.name, table.ru.get(ru.name).pos));
                } else if (!table.en.containsKey(en.name)) {
                    table.addRu(ru);
                    table.addEn(en);
                }
            }
        }

        table.generateDictionary("dictionary3.txt");
    }

    static void updateTable() {
        var table = new Table();
        table.loadDictionary("dictionary.txt");

        var table2 = new Table(InvSprite);
        table2.loadData(ru);
        table2.loadData(en);
        table2.loadDictionary();

        var table3 = new Table();

        table2.en.values().forEach(table3::addEn);

        for (var entry : table2.ru.entrySet()) {
            if (!table.ru.containsKey(entry.getKey())) {
                var word = entry.getValue();
                table3.addRu(new Word(word.name, word.pos, word.section, true));
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
                    table3.addRu(new Word(name, word.pos, word.section));
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
                    table3.addRu(new Word(word.name, word.pos, word.section));
                }
            }
        }

        table3.generateOutput(Word.nameComparator);
    }

    static String work(String code) {

        String[] types = {
                "Предмет",
                "Блок",
                "Сущность",
                "Биом",
                "Окружение"
        };

        for (String type : types) {
            for (int i = 0; ; ) {
                int a = code.indexOf("{{Спрайт/" + type + "|", i);
                if (a < 0) break;

                i = a + type.length() + 10;

                int b = code.indexOf("}}", i);
                String mt = code.substring(i, b).replace('-', ' ').replace('_', ' ').toLowerCase();

                int c = code.indexOf("[[", b) + 2;
                if (c < 2) break;
                if (b + 6 < c) {
                    continue;
                }

                int d = code.indexOf("]]", c);
                String mp = code.substring(c, d);

//                System.out.println(mt + " " + mp);

                if (mt.equals(mp.replace('-', ' ').toLowerCase())) {
                    code = code.substring(0, a) + "{{Ссылка/" + type + "|" + mp + "}}" + code.substring(d + 2);
                }
            }
        }

        return code;
    }
}
