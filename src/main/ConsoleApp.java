package main;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import static java.lang.System.*;
import static main.Lang.en;
import static main.Lang.ru;

public class ConsoleApp {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setProperty("console.encoding", "utf-8");
        String encoding = System.getProperty("console.encoding", "utf-8");
        System.setOut(new PrintStream(System.out, true, encoding));

        if (args.length == 0) {
            availableCommands();
            return;
        }

        switch (args[0]) {
            case "translate": translate(args); break;
            case "generateDictionary": generateDictionary(args); break;
            case "generateTable": generateTable(args); break;
            default: availableCommands();
        }
    }

    private static void availableCommands() {
        out.println("Available commands:");
        out.println("translate");
        out.println("generateDictionary");
        out.println("generateTable");
    }

    private static void translate(String[] args) {
        if (args.length != 3) {
            out.println("Usage: translate <input_file> <output_file>");
            out.println("Example: translate input.txt output.txt");
            return;
        }

        Main.translate(args[1], args[2]);
    }

    private static void generateDictionary(String[] args) {
        if (args.length == 1 || args.length > 3) {
            generateDictionaryError();
            return;
        }

        int maxID = 10000;
        if (args.length >= 3) {
            try {
                maxID = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                out.println("Wrong max_lines_count value: " + args[2]);
                generateDictionaryError();
                return;
            }

            if (maxID < 0 || maxID > 100000) {
                out.println("Wrong max_lines_count value: " + args[2]);
                generateDictionaryError();
                return;
            }
        }

        var table = new Table(args[1], maxID);
        table.loadData(en);
        table.loadData(ru);
        table.generateDictionary();
    }

    private static void generateTable(String[] args) {
        if (args.length == 1 || args.length > 4) {
            generateTableError();
            return;
        }

        int maxID = 10000;
        if (args.length >= 3) {
            try {
                maxID = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                out.println("Wrong max_lines_count value: " + args[2]);
                generateTableError();
                return;
            }

            if (maxID < 0 || maxID > 100000) {
                out.println("Wrong max_lines_count value: " + args[2]);
                generateTableError();
                return;
            }
        }

        var sort_by = "name";
        if (args.length >= 4) {
            if (args[3].equals("pos")) {
                sort_by = "pos";
            } else if (!args[3].equals("name")) {
                out.println("Wrong sort_by value: " + args[3]);
                generateTableError();
                return;
            }
        }

        var table = new Table(args[1], maxID);
        table.loadData(en);
        table.loadData(ru);
        table.loadDictionary();
        table.generateOutput(sort_by.equals("pos") ? Word::compareByPos : Word::compareByName);
    }

    private static void generateDictionaryError() {
        out.println("Usage: generateDictionary <directory> [max_lines_count]");
        out.println("Example: generateDictionary InvSprite 5000");
    }

    private static void generateTableError() {
        out.println("Usage: generateTable <directory> [max_lines_count] [sort_by (name|pos)]");
        out.println("Example: generateTable InvSprite 5000 name");
    }
}
