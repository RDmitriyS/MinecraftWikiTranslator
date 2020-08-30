package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
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

    static String lowerTypeToUpperType(String val, Lang lang) {
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

    static String lowerTypeToUpperTypeEN(String val) {
        return lowerTypeToUpperType(val, Lang.en);
    }

    static String lowerTypeToUpperTypeRU(String val) {
        return lowerTypeToUpperType(val, Lang.ru);
    }

    static String upperTypeToLowerType(String val) {
        return val.toLowerCase().replace(' ', '-');
    }

    static Map<String, String> to_lower_upper_map(final Set<String> set) {
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
            String encoding = System.getProperty("console.encoding", "utf-8");
            return new Scanner(new File(filename), encoding);
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
        final var builder = new StringBuilder();
        while (reader1.hasNextLine() && reader2.hasNextLine()) {
            appendAll(builder, reader1.nextLine(), reader2.nextLine(), '\n');
        }
        write("output.txt", builder.toString());
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
        final char[] lc = lhs.toCharArray();
        final char[] rc = rhs.toCharArray();
        final int lim = Math.min(lc.length, rc.length);
        for (int k = 0; k < lim; k++) {
            if (lc[k] != rc[k]) {
                if (lc[k] == 'ё') {
                    return rc[k] == 'е' ? 1 : 'е' - rc[k];
                } else if (rc[k] == 'ё') {
                    return lc[k] == 'е' ? -1 : lc[k] - 'е';
                } else if (lc[k] == 'Ё') {
                    return rc[k] == 'Е' ? 1 : 'Е' - rc[k];
                } else if (rc[k] == 'Ё') {
                    return lc[k] == 'Е' ? -1 : lc[k] - 'Е';
                } else {
                    return lc[k] - rc[k];
                }
            }
        }
        return lc.length - rc.length;
    }
}
