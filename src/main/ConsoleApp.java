package main;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static java.lang.System.*;
import static main.Lang.en;
import static main.Lang.ru;
import static main.Table.showWarnings;

public class ConsoleApp {

    public static void main(String[] args) throws UnsupportedEncodingException {
        String encoding = System.getProperty("console.encoding", "utf-8");
        System.setOut(new PrintStream(System.out, true, encoding));

        if (args.length == 0) {
            availableArguments();
            return;
        }

        switch (args[0]) {
            case "translate":
                translate(args);
                break;
            case "generateDictionary":
                generateDictionary(args);
                break;
            case "generateTable":
                generateTable(args);
                break;
            default:
                availableArguments();
        }
    }

    private static void availableArguments() {
        out.println("Available arguments:");
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
        if (args.length != 2) {
            generateDictionaryError();
            return;
        }

        var table = new Table(args[1]);
        table.loadData(en);
        table.loadData(ru);
        table.generateDictionary();
    }

    private static void generateTable(String[] args) {
        if (args.length == 1 || args.length > 3) {
            generateTableError();
            return;
        }

        var sortBy = "name";
        if (args.length >= 3) {
            if (args[2].equals("pos")) {
                sortBy = "pos";
            } else if (!args[2].equals("name")) {
                out.println("Illegal value of argument 3: " + args[2]);
                generateTableError();
                return;
            }
        }

        var table = new Table(args[1]);
        table.loadData(en);
        table.loadData(ru);
        showWarnings = false;
        table.loadDictionary();
        table.generateOutput(sortBy.equals("pos") ? Word.posComparator : Word.nameComparator);
    }

    private static void generateDictionaryError() {
        out.println("Usage: generateDictionary <directory>");
        out.println("Example: generateDictionary InvSprite");
    }

    private static void generateTableError() {
        out.println("Usage: generateTable <directory> [sort_by (name|pos)]");
        out.println("Example: generateTable InvSprite name");
    }
}
