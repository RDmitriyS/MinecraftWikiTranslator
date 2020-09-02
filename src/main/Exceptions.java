package main;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static main.Functions.to_lower_upper_map;

public class Exceptions {

    final static Set<String> word_exceptions = Set.of(
            "a", "the", "on", "and", "with", "o'", "of",
            "BE", "LCE", "PE", "CE", "Edition", "Bedrock", "Education",
            "Края", "Энд", "Энда", "Эндера", "Нижнего", "Незера",
            "ТНТ", "чем-то"
    );

    final static Set<String> name_exceptions = Set.of(
            "USB-блок",
            "Блок-конструктор",
            "Ведро с рыбой-клоуном",
            "Лук-батун",
            "Блок-пазл",
            "Рыба-клоун",
            "Какао-бобы",
            "Светильник Джека",
            "Jack o'Lantern",
            "Alkali metal",
            "Alkaline earth metal",
            "Noble gas",
            "Other non-metal",
            "Post transition metal",
            "Transition metal"
    );

    final static Map<String, String> word_exceptions_lower = to_lower_upper_map(word_exceptions);
    final static Map<String, String> name_exceptions_lower = to_lower_upper_map(name_exceptions);

    final static Set<String> dash_exceptions = Set.of(
            "светло-", "pre-", "визер-", "верхне-", "нижне-",
            "эндер-", "тёмно-", "сундук-ловушк", "кожано-", "незер-",
            "зомби-жител", "зомби-крестьянин", "зомби-свиночеловек",
            "лошади-зомби", "лошади-скелет", "рыбы-клоун", "скелета-иссушител"
    );

    final static HashSet<Character> illegal_start_end_chars = new HashSet<>();

    final static Set<String> ignored_ru_words = Set.of(
//            "-бок", "-перед", "-вниз", "-верх", "-низ", "-ножка", "-вертикальные", "-прямые",
//            "-север", "-юг", "-запад", "-восток", "-старая", "-старое", "-старый", "-старые",
//            "pre-", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9",
//            "-вкл", "-выкл",
//            "-be-", "-ce-", "-pe-",
//            " BE", " LCE", " PE",
//            " блок ", "Блок"
//            " (версия 1)", " (версия 2)", " (версия 3)", " (предмет)
    );

}
