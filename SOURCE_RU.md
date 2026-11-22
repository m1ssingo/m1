# Исходники M1 SciFi 0.10.1

Репозиторий содержит настройки сборки, квесты, ресурсы и код собственных модов. Готовые комплекты передаются отдельными архивами. [Установка игры](README_RU.md)

| Путь | Содержимое |
| --- | --- |
| `mods.lock.json` | Minecraft 1.21.1, NeoForge 21.1.267, стороны установки, URL и контрольные суммы модов |
| `overrides/` | Общие файлы клиента и сервера. Квесты лежат в `config/ftbquests/quests/`, этапы в `config/historystages/global/`, рецепты и достижения в `kubejs/data/`. |
| `compat/` | `m1packcompat` 0.2.4. Блокировка базовой схемы Mekanism, совместимость Golem Core и History Stages, переходы Mekanism и проверка смены Colony Core. |
| `client/` | `m1packclient` 0.4.5. Оформление меню и кнопок, клавиши, открытие рецепта из задачи FTB Quests и обзор главы. |
| `server-template/` | Шаблон выделенного сервера Windows |
| `testing/`, `tools/` | Диагностика и сборка архивов |

Исходники двух собственных модов распространяются по [MIT](compat/LICENSE). Заставка и дорожка Ignition предоставлены авторами сборки. Регистрация книги находится в [ntm.json](overrides/kubejs/assets/m1prototype/guideme_guides/ntm.json), страницы и 3D-сцены — в [каталоге GuideME](overrides/kubejs/assets/m1prototype/guides/m1prototype/ntm/index.md).

## Повторная упаковка 0.10.1

Для упаковки выпущенной версии используйте Python 3.11 или новее и два JAR из `M1-SciFi-Prototype-0.10.1.zip`.

1. Из `server/overrides/mods/` полного архива извлеките `m1packcompat-0.2.4.jar` в `overrides/mods/` рабочего дерева.
2. Откройте вложенный `client/M1-SciFi-Prototype-0.10.1-CurseForge.zip`. Из его `overrides/mods/` извлеките `m1packclient-0.4.5.jar` в `client-overrides/mods/` рабочего дерева.
3. Из корня рабочего дерева выполните `python tools/build_release.py --output dist`. При необходимости передайте `--verification` с JSON для версии 0.10.1, например из `evidence/verification.json` полного архива.

Упаковщик проверяет суммы SHA-256 обоих JAR по lock-файлу и сверяет общие файлы клиентского и серверного комплектов. [Список модов](docs/MODS.md)

## Сборка собственных модов

Нужны Java 21, Python 3.11 или новее и библиотеки установленного NeoForge. Для сборки `m1packclient` нужны также Pillow и NumPy. Для classpath используйте папку `mods` профиля с зафиксированными версиями.

- `python tools/build_compat.py --libraries PATH_TO_SERVER_LIBRARIES --mods PATH_TO_MODS --java PATH_TO_JAVA_EXE`
- `python tools/build_client.py --animation PATH_TO_BACKGROUND_M1A --libraries PATH_TO_CLIENT_LIBRARIES --mods PATH_TO_MODS --java PATH_TO_JAVA_EXE`

Перед сборкой клиента извлеките из `m1packclient-0.4.4.jar` выпуска 0.10.0 звуковой файл `assets/m1packclient/sounds/ignition.ogg` в `client/resources/assets/m1packclient/sounds/`. Файлы `background.m1a` и `background.png` из `assets/m1packclient/textures/menu/` положите рядом друг с другом и передайте путь к `.m1a`. Изображение ракеты хранится отдельно в `client/resources/assets/m1packclient/textures/menu/rocket.png` и входит в JAR при сборке. Упаковщик анимации также принимает GIF или WebP из 250 кадров 1672×941 по 80 мс.

При упаковке изменённых JAR обновите их SHA-256 в lock-файле рабочего дерева. `tools/resolve.py download` получает версии из lock-файла и записывает метаданные и суммы по скачанным JAR. Для повторной упаковки выпущенной версии достаточно исходного lock-файла и двух JAR из её архива.

## Диагностика

- `m1probe` выводит состояние этапа электроники и видимость двух рецептов. Команда работает в установленной сборке.
- `testing/quest_polish_fixture.js` проверяет связи 334 заданий, справочные ссылки и семь наград исследований. Для запуска скопируйте скрипт в `kubejs/server_scripts/` одноразового сервера и выполните `m1questpolishfixture` в консоли.
- `tools/smoke_server.py` предназначен для предварительно подготовленного сервера в `work/server-test`. Параметры `--industry` и `--quest-book` запускают отдельные проверки, часть которых рассчитана на старые наборы глав. `tools/client_smoke.py` предназначен для графического стенда Linux с Xorg.
- `testing/disposable-datapack`, `testing/industry_fixture.js` и `testing/interplanetary_fixture.js` размещают блоки и создают тестовые данные. Подключайте их только в одноразовых мирах. Игровые архивы содержат эти файлы в исходниках для диагностики.
- `tools/PcmProbe.java` проверяет AAC-декодер OpenFM с JAAD в classpath. Для него нужен свой тестовый аудиофрагмент.

[Диагностика по наблюдениям в игре](docs/TEST_PROTOCOL_RU.md) · [Статус сборки](docs/STATUS_RU.md)
