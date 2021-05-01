package main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static main.Exceptions.*;

public class Functions {

    static String lowerTypeToUpperType(String val, Lang lang) {
        var temp = NAME_EXCEPTIONS_LOWER.get(val);
        if (temp != null) {
            return temp;
        }

        final var sb = new StringBuilder(val);

        if (lang == Lang.ru) {
            for (var key : DASH_EXCEPTIONS) {
                final int index = sb.indexOf(key);
                if (index >= 0) {
                    sb.setCharAt(sb.indexOf("-", index), '+');
                }
            }
        }

        for (int end = 0, begin = 0; end <= sb.length(); end++) {
            if (end == sb.length() || sb.charAt(end) == '-') {
                final var newWord = WORD_EXCEPTIONS_LOWER.get(sb.substring(begin, end));
                if (newWord != null) {
                    sb.replace(begin, end, newWord);
                } else if (lang == Lang.en || begin == 0) {
                    sb.setCharAt(begin, Character.toUpperCase(sb.charAt(begin)));
                }
                begin = end + 1;
            }
        }

        return sb.toString().replace('-', ' ').replace('+', '-');
    }

    static String lowerTypeToUpperTypeEN(String val) {
        return lowerTypeToUpperType(val, Lang.en);
    }

    static String lowerTypeToUpperTypeRU(String val) {
        return lowerTypeToUpperType(val, Lang.ru);
    }

    static String upperTypeToLowerType(String val) {
        return val.toLowerCase().replace(' ', '-');
    }

    static Map<String, String> toLowerUpperMap(Set<String> set) {
        return set.stream().collect(Collectors.toMap(Functions::upperTypeToLowerType, word -> word));
    }

    static String readFile(String path) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    static String readHeader(String path) {
        FileReader fr;
        try {
            fr = new FileReader(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        var br = new BufferedReader(fr);
        var builder = new StringBuilder();
        var lines = br.lines().iterator();
        var line = "";
        do {
            line = lines.next();
            appendAll(builder, line, "\n");
        } while (!(line.contains("['IDы']") || line.contains("ids")));

        return builder.toString();
    }

    static void write(final String file, final String text){
        try (var writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.write(text);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static Scanner getScanner(Path path) {
        try {
            String encoding = System.getProperty("console.encoding", "utf-8");
            return new Scanner(path, encoding);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    static void appendAll(StringBuilder sb, final Object... array) {
        for (var value : array) {
            sb.append(value);
        }
    }

    static void mergeByLine(String path1, String path2) {
        final var reader1 = getScanner(Path.of(path1));
        final var reader2 = getScanner(Path.of(path2));
        final var builder = new StringBuilder();
        while (reader1.hasNextLine() && reader2.hasNextLine()) {
            appendAll(builder, reader1.nextLine(), reader2.nextLine(), '\n');
        }
        write("output.txt", builder.toString());
    }

    static <T> T getFirstElse(Collection<T> col, T value) {
        return col.isEmpty() ? value : col.iterator().next();
    }
}
