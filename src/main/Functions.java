package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Functions {

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

    static Map<String, String> to_lower_upper_map(final Set<String> set) {
        return set.stream().collect(Collectors.toMap(Main::toLowerCase, word -> word));
    }
}
