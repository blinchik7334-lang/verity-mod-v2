# Verity Transformation (Fabric 1.20.1)

Команда `/verity` — превращает тебя в Verity (повторно — вернуть обратно).
`/verity <ник>` — превратить другого игрока (повторно — вернуть обратно).
Команда доступна ТОЛЬКО создателю. Ник создателя пишется в файле
`config/verity-owner.txt` (создаётся при первом запуске игры/сервера) — пересборка не нужна.
Облик видят все игроки, у которых установлен мод.

## Как собрать
1. Нужны JDK 17 и Gradle 8.x (или открой папку в IntelliJ IDEA — всё подтянется само).
2. `gradle build`
3. Готовый мод: `build/libs/verity-1.0.0.jar` → в папку `mods` (нужен Fabric Loader + Fabric API).

## Свой скин Verity
Замени файл `src/main/resources/assets/verity/textures/entity/verity.png`
на настоящий скин (PNG 64x64, формат как у обычного скина Minecraft) и пересобери.
Сейчас там простая цветная заглушка.

## 3D-модели и как добавить свою
Команды (только для создателя):
- `/verity` — вкл/выкл форму по умолчанию на себе
- `/verity <ник>` — вкл/выкл на другом игроке
- `/verity form <id> [ник]` — выбрать конкретную модель (`smile` — добрый, `evil` — злой, `talk` — говорящий)

Добавить новую модель:
1. Скопируй `client/SmileModel.java` → `MyModel.java`, измени боксы (`cuboid`) под свою форму.
2. Положи текстуру в `assets/verity/textures/entity/my.png` (размер = числа в `TexturedModelData.of`).
3. В `client/VerityModels.registerAll()` добавь `add(new Form("my", ...))`.
4. В `VerityForms.IDS` добавь `"my"`.
Моделям из Blockbench (формат Java Block/Entity → "Modded Entity", Yarn) код можно переносить почти как есть.

## Говорящий Verity (`/verity form talk`)
Рот открывается, когда игрок:
- пишет в чат (чем длиннее сообщение, тем дольше «говорит»);
- говорит в микрофон — для этого на сервере и у игроков должен стоять мод
  **Simple Voice Chat** (https://modrinth.com/plugin/simple-voice-chat). Мод Verity сам к микрофону
  не обращается: он только получает от Simple Voice Chat сигнал «игрок говорит».
Чтобы другая форма тоже «говорила», передай вторую текстуру (рот открыт) в `new Form(...)` вместо `null`.

## Круглый шар
Формы smile / evil / talk рисуются как настоящий гладкий шар (`client/SphereMesh.java`).
Чтобы сделать новую форму-шар, достаточно нарисовать текстуру (лицо 18x18 в точке 24,24 на
жёлтом фоне 128x128) и добавить строку `add(new Form("my", tex("my_ball"), null, null, null, 1.0f))`.
