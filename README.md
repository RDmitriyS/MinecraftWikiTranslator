# MinecraftWikiTranslator
## Применение
Использует таблицы спрайтов с Minecraft Wiki в следующих целях:
* перевод текста с английского на русский
  - синтаксис: `translate [входной файл] [выходной файл]`
  - пример: `java -jar Translator.jar translate input.txt output.txt`
  - `входной файл` и `выходной файл` по умолчанию `input.txt` и `output.txt`
* создание словаря по таблицам с англо- и русскоязычного раздела
  - синтаксис: `generateDictionary <директория>`
  - пример: `java -jar Translator.jar generateDictionary InvSprite`
  - таблицы находятся в файлах `<директория>/en.txt` и `<директория>/ru.txt`
  - словарь помещается в файл `<директория>/dictionary.txt`
  - `generateAllDictionaries` сгенерирует все словари 
* создание русскоязычной таблицы по словарю
  - синтаксис: `generateTable <директория> [сортировать по (name|pos)]`
  - пример: `java -jar Translator.jar generateTable InvSprite name`
  - таблица помещается в файл `<директория>/table.txt`
  - `generateAllTables` сгенерирует все таблицы
* загрузка таблиц с вики
  - синтаксис: `updateContent <директория>`
  - пример: `java -jar Translator.jar updateContent InvSprite`
  - обновляет файлы `<директория>/en.txt` и `<директория>/ru.txt`
  - `updateAllContent` обновляет файлы во всех директориях

## Как читать словарь
В строчке с номером `i` находится перевод спрайта с индексом `i`.\
В конце перевода дописывается `|у`, если этот перевод устарел (например, `[Берёза|у]`).

Словари в директории `Default` содержат все известные однозначные переводы, то есть каждому английскому названию соответствует единственное русское.