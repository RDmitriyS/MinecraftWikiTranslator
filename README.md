# MinecraftWikiTranslator
Использует таблицы спрайтов с Minecraft Wiki в следующих целях:
* перевод текста с английского на русский
  - синтаксис: `translate <входной файл> <выходной файл>`
  - пример: `java -jar MinecraftWikiTranslator.jar translate input.txt output.txt`
* создание словаря по таблицам с англо- и русскоязычного раздела
  - синтаксис: `generateDictionary <директория>`
  - пример: `java -jar MinecraftWikiTranslator.jar generateDictionary InvSprite`
* создание русскоязычной таблицы по словарю
  - синтаксис: `generateTable <директория> [сортировать по (name|pos)]`
  - пример: `java -jar MinecraftWikiTranslator.jar generateTable InvSprite name`
