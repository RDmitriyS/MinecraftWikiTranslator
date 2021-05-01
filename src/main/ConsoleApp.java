package main;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;

import static java.lang.System.*;
import static main.Lang.en;
import static main.Lang.ru;

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
            case "generateAllDictionaries":
                generateAllDictionaries(args);
                break;
            case "generateTable":
                generateTable(args);
                break;
            case "generateAllTables":
                generateAllTables(args);
                break;
            case "updateContent":
                updateContent(args);
                break;
            case "updateAllContent":
                updateAllContent(args);
                break;
            default:
                availableArguments();
        }
    }

    private static void availableArguments() {
        out.println("Available arguments:");
        out.println("translate");
        out.println("generateDictionary");
        out.println("generateAllDictionaries");
        out.println("generateTable");
        out.println("generateAllTables");
        out.println("updateContent");
        out.println("updateAllContent");
    }

    private static void translate(String[] args) {
        if (args.length > 3) {
            out.println("Usage: translate [input_file] [output_file]");
            out.println("Example: translate input.txt output.txt");
            return;
        }

        if (args.length == 1) {
            Main.translate();
        } else if (args.length == 2) {
            Main.translate(args[1], null);
        } else {
            Main.translate(args[1], args[2]);
        }
    }

    private static void generateDictionary(String[] args) {
        if (args.length != 2) {
            generateDictionaryError();
            return;
        }

        Main.generateDictionary(args[1]);
    }

    private static void generateAllDictionaries(String[] args) {
        if (args.length > 2) {
            out.println("Usage: generateAllDictionaries");
            return;
        }

        Main.generateAllDictionaries();
    }

    private static void generateTable(String[] args) {
        if (args.length == 1 || args.length > 3) {
            generateTableError();
            return;
        }

        Comparator<Word> comp = null;
        if (args.length >= 3) {
            if (args[2].equals("pos")) {
                comp = Word.posComparator;
            } else if (args[2].equals("name")) {
                comp = Word.nameComparator;
            } else {
                out.println("Illegal value of argument 3: " + args[2]);
                generateTableError();
                return;
            }
        }

        Main.generateTable(args[1], comp);
    }

    private static void generateAllTables(String[] args) {
        if (args.length > 2) {
            out.println("Usage: generateAllTables");
            return;
        }

        Main.generateAllTables();
    }

    private static void updateContent(String[] args) {
        if (args.length == 1 || args.length > 3) {
            out.println("Usage: updateContent <directory>");
            out.println("Example: updateContent InvSprite");
            return;
        }

        Main.updateContent(ru, TableType.valueOf(args[1]));
        Main.updateContent(en, TableType.valueOf(args[1]));
    }

    private static void updateAllContent(String[] args) {
        if (args.length > 2) {
            out.println("Usage: updateAllContent");
            return;
        }

        Main.updateAllContent();
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
