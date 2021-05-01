package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Settings {
    final boolean translateLowercaseWords;

    Settings() {
        var map = new HashMap<String, String>();

        try (var br = new BufferedReader(new FileReader("translate_settings.txt"))) {
            br.lines().forEach(line -> {
                var array = line.split("=");
                map.put(array[0], array[1]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        translateLowercaseWords = "true".equals(map.get("translate-lowercase-words"));
    }
}
