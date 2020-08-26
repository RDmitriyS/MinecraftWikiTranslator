package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static main.Exceptions.*;

public class Functions {

    static String toUpperCase(String val, Lang lang) {
        var temp = name_exceptions_lower.get(val);
        if (temp != null) {
            return temp;
        }

        final var sb = new StringBuilder(val);

        if (lang == Lang.ru) {
            for (var key : dash_exceptions) {
                final int index = sb.indexOf(key);
                if (index >= 0) {
                    sb.setCharAt(sb.indexOf("-", index), '+');
                }
            }
        }

        for (int end = 0, begin = 0; end <= sb.length(); end++) {
            if (end == sb.length() || sb.charAt(end) == '-') {
                final var new_word = word_exceptions_lower.get(sb.substring(begin, end));
                if (new_word != null) {
                    sb.replace(begin, end, new_word);
                } else if (lang == Lang.en || begin == 0) {
                    sb.setCharAt(begin, Character.toUpperCase(sb.charAt(begin)));
                }
                begin = end + 1;
            }
        }

        return sb.toString().replace('-', ' ').replace('+', '-');
    }

    static String toLowerCase(String val) {
        return val.toLowerCase().replace(' ', '-');
    }

    static Map<String, String> to_lower_upper_map(final Set<String> set) {
        return set.stream().collect(Collectors.toMap(Functions::toLowerCase, word -> word));
    }

    static String readFile(String path) {
        byte[] encoded = null;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    static void write(final String file, final String text) {
        Path path = Paths.get(file);
        try {
            Files.write(path, text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Scanner getScanner(String filename) {
        try {
            return new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
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
        final var reader1 = getScanner(path1);
        final var reader2 = getScanner(path2);
        while (reader1.hasNextLine() && reader2.hasNextLine()) {
            write("output.txt", (reader1.nextLine() + reader2.nextLine()));
        }
    }

    static void requireTrue(final boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    static <T> T getFirstElse(Collection<T> col, T value) {
        return col.isEmpty() ? value : col.iterator().next();
    }

    static <T> T getFirstIfNotNullElse(T first, T second) {
        return first != null ? first : second;
    }

    static int compareTo(final String lhs, final String rhs) {
        return lhs.replace('ё', 'е').compareTo(rhs.replace('ё', 'е'));
    }
}
