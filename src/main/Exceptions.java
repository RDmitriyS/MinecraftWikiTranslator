package main;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static main.Functions.toLowerUpperMap;

public class Exceptions {

    final static Set<String> WORD_EXCEPTIONS = Set.of(
            "a", "the", "on", "and", "with", "o'", "of",
            "BE", "LCE", "PE", "CE", "Edition", "Bedrock", "Education",
            "Края", "Энд", "Энда", "Эндера", "Нижнего", "Незера",
            "ТНТ", "чем-то"
    );

    final static Set<String> NAME_EXCEPTIONS = Set.of(
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

    final static Map<String, String> WORD_EXCEPTIONS_LOWER = toLowerUpperMap(WORD_EXCEPTIONS);
    final static Map<String, String> NAME_EXCEPTIONS_LOWER = toLowerUpperMap(NAME_EXCEPTIONS);

    final static Set<String> DASH_EXCEPTIONS = Set.of(
            "светло-", "pre-", "визер-", "верхне-", "нижне-",
            "эндер-", "тёмно-", "сундук-ловушк", "кожано-", "незер-",
            "зомби-жител", "зомби-крестьянин", "зомби-свиночеловек",
            "лошади-зомби", "лошади-скелет", "рыбы-клоун", "скелета-иссушител"
    );

    final static HashSet<Character> ILLEGAL_START_END_CHARS = new HashSet<>();

    final static Set<String> IGNORED_RU_WORDS = Set.of(
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
