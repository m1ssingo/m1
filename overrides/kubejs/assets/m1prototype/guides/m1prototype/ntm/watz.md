---
navigation:
  title: Ватцз
  icon: hbm:struct_watz_core
  parent: reactors.md
  position: 7
item_ids:
  - hbm:watz
  - hbm:struct_watz_core
---

# Ватцз

Ватцз греет хладагент и перерабатывает топливные гранулы. В отдельный бак уходят токсичные отходы.

## Сборка

1. Поставь **Ядро Ватцз-реактора** в `(0, 0, 0)`. Уложи блоки на трёх уровнях по модели. Для корпуса нужны именно заклёпанные стабилизаторы. Собранная конструкция сама превратится в корпус реактора.
2. Поставь **Нагнетающую помпу (Ватцз)** в `(0, 3, 0)`, над ядром. В месте `(0, 5, 0)` подводится сигнал редстоуна к помпе. Для первой проверки оставь его выключенным.
3. Подведи **Хладагент** к жидкостному порту. Выдели отдельные трубы и баки для **Горячего хладагента** и **Токсичных отходов**. Используй свободные порты сверху или снизу по [схеме собранного корпуса](./layout-reactor.md).
4. Проверь заполнение входа и маршрут каждого выхода. На рабочей раскладке с гранулами подходящего типа пуск выполняется сигналом на помпе. При остановке сними сигнал и оставь отвод хладагента, пока температура снижается.

На сервере собрана конструкция из блоков по этой карте, установлена помпа и сверены типы баков.

## Детали для камеры

| Блок | Количество |
| --- | ---: |
| Ядро Ватцз-реактора | 1 |
| Суперохладитель реактора (Ватцз) | 26 |
| Камера реакции (Ватцз) | 36 |
| Заклёпанный стабилизатор реактора (Ватцз) | 48 |

<GameScene zoom="1.7" interactive={true}>
  <IsometricCamera yaw="135" pitch="35" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="0" z="-1" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="0" z="0" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="0" z="1" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="1" z="-1" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="1" z="0" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="1" z="1" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="2" z="-1" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="2" z="0" />
  <Block id="hbm:watz_casing_tooled" x="-3" y="2" z="1" />
  <Block id="hbm:watz_casing_tooled" x="-2" y="0" z="-2" />
  <Block id="hbm:watz_cooler" x="-2" y="0" z="-1" />
  <Block id="hbm:watz_element" x="-2" y="0" z="0" />
  <Block id="hbm:watz_cooler" x="-2" y="0" z="1" />
  <Block id="hbm:watz_casing_tooled" x="-2" y="0" z="2" />
  <Block id="hbm:watz_casing_tooled" x="-2" y="1" z="-2" />
  <Block id="hbm:watz_cooler" x="-2" y="1" z="-1" />
  <Block id="hbm:watz_element" x="-2" y="1" z="0" />
  <Block id="hbm:watz_cooler" x="-2" y="1" z="1" />
  <Block id="hbm:watz_casing_tooled" x="-2" y="1" z="2" />
  <Block id="hbm:watz_casing_tooled" x="-2" y="2" z="-2" />
  <Block id="hbm:watz_cooler" x="-2" y="2" z="-1" />
  <Block id="hbm:watz_element" x="-2" y="2" z="0" />
  <Block id="hbm:watz_cooler" x="-2" y="2" z="1" />
  <Block id="hbm:watz_casing_tooled" x="-2" y="2" z="2" />
  <Block id="hbm:watz_casing_tooled" x="-1" y="0" z="-3" />
  <Block id="hbm:watz_cooler" x="-1" y="0" z="-2" />
  <Block id="hbm:watz_element" x="-1" y="0" z="-1" />
  <Block id="hbm:watz_element" x="-1" y="0" z="0" />
  <Block id="hbm:watz_element" x="-1" y="0" z="1" />
  <Block id="hbm:watz_cooler" x="-1" y="0" z="2" />
  <Block id="hbm:watz_casing_tooled" x="-1" y="0" z="3" />
  <Block id="hbm:watz_casing_tooled" x="-1" y="1" z="-3" />
  <Block id="hbm:watz_cooler" x="-1" y="1" z="-2" />
  <Block id="hbm:watz_element" x="-1" y="1" z="-1" />
  <Block id="hbm:watz_element" x="-1" y="1" z="0" />
  <Block id="hbm:watz_element" x="-1" y="1" z="1" />
  <Block id="hbm:watz_cooler" x="-1" y="1" z="2" />
  <Block id="hbm:watz_casing_tooled" x="-1" y="1" z="3" />
  <Block id="hbm:watz_casing_tooled" x="-1" y="2" z="-3" />
  <Block id="hbm:watz_cooler" x="-1" y="2" z="-2" />
  <Block id="hbm:watz_element" x="-1" y="2" z="-1" />
  <Block id="hbm:watz_element" x="-1" y="2" z="0" />
  <Block id="hbm:watz_element" x="-1" y="2" z="1" />
  <Block id="hbm:watz_cooler" x="-1" y="2" z="2" />
  <Block id="hbm:watz_casing_tooled" x="-1" y="2" z="3" />
  <Block id="hbm:watz_casing_tooled" x="0" y="0" z="-3" />
  <Block id="hbm:watz_element" x="0" y="0" z="-2" />
  <Block id="hbm:watz_element" x="0" y="0" z="-1" />
  <Block id="hbm:struct_watz_core" x="0" y="0" z="0" />
  <Block id="hbm:watz_element" x="0" y="0" z="1" />
  <Block id="hbm:watz_element" x="0" y="0" z="2" />
  <Block id="hbm:watz_casing_tooled" x="0" y="0" z="3" />
  <Block id="hbm:watz_casing_tooled" x="0" y="1" z="-3" />
  <Block id="hbm:watz_element" x="0" y="1" z="-2" />
  <Block id="hbm:watz_element" x="0" y="1" z="-1" />
  <Block id="hbm:watz_cooler" x="0" y="1" z="0" />
  <Block id="hbm:watz_element" x="0" y="1" z="1" />
  <Block id="hbm:watz_element" x="0" y="1" z="2" />
  <Block id="hbm:watz_casing_tooled" x="0" y="1" z="3" />
  <Block id="hbm:watz_casing_tooled" x="0" y="2" z="-3" />
  <Block id="hbm:watz_element" x="0" y="2" z="-2" />
  <Block id="hbm:watz_element" x="0" y="2" z="-1" />
  <Block id="hbm:watz_cooler" x="0" y="2" z="0" />
  <Block id="hbm:watz_element" x="0" y="2" z="1" />
  <Block id="hbm:watz_element" x="0" y="2" z="2" />
  <Block id="hbm:watz_casing_tooled" x="0" y="2" z="3" />
  <Block id="hbm:watz_casing_tooled" x="1" y="0" z="-3" />
  <Block id="hbm:watz_cooler" x="1" y="0" z="-2" />
  <Block id="hbm:watz_element" x="1" y="0" z="-1" />
  <Block id="hbm:watz_element" x="1" y="0" z="0" />
  <Block id="hbm:watz_element" x="1" y="0" z="1" />
  <Block id="hbm:watz_cooler" x="1" y="0" z="2" />
  <Block id="hbm:watz_casing_tooled" x="1" y="0" z="3" />
  <Block id="hbm:watz_casing_tooled" x="1" y="1" z="-3" />
  <Block id="hbm:watz_cooler" x="1" y="1" z="-2" />
  <Block id="hbm:watz_element" x="1" y="1" z="-1" />
  <Block id="hbm:watz_element" x="1" y="1" z="0" />
  <Block id="hbm:watz_element" x="1" y="1" z="1" />
  <Block id="hbm:watz_cooler" x="1" y="1" z="2" />
  <Block id="hbm:watz_casing_tooled" x="1" y="1" z="3" />
  <Block id="hbm:watz_casing_tooled" x="1" y="2" z="-3" />
  <Block id="hbm:watz_cooler" x="1" y="2" z="-2" />
  <Block id="hbm:watz_element" x="1" y="2" z="-1" />
  <Block id="hbm:watz_element" x="1" y="2" z="0" />
  <Block id="hbm:watz_element" x="1" y="2" z="1" />
  <Block id="hbm:watz_cooler" x="1" y="2" z="2" />
  <Block id="hbm:watz_casing_tooled" x="1" y="2" z="3" />
  <Block id="hbm:watz_casing_tooled" x="2" y="0" z="-2" />
  <Block id="hbm:watz_cooler" x="2" y="0" z="-1" />
  <Block id="hbm:watz_element" x="2" y="0" z="0" />
  <Block id="hbm:watz_cooler" x="2" y="0" z="1" />
  <Block id="hbm:watz_casing_tooled" x="2" y="0" z="2" />
  <Block id="hbm:watz_casing_tooled" x="2" y="1" z="-2" />
  <Block id="hbm:watz_cooler" x="2" y="1" z="-1" />
  <Block id="hbm:watz_element" x="2" y="1" z="0" />
  <Block id="hbm:watz_cooler" x="2" y="1" z="1" />
  <Block id="hbm:watz_casing_tooled" x="2" y="1" z="2" />
  <Block id="hbm:watz_casing_tooled" x="2" y="2" z="-2" />
  <Block id="hbm:watz_cooler" x="2" y="2" z="-1" />
  <Block id="hbm:watz_element" x="2" y="2" z="0" />
  <Block id="hbm:watz_cooler" x="2" y="2" z="1" />
  <Block id="hbm:watz_casing_tooled" x="2" y="2" z="2" />
  <Block id="hbm:watz_casing_tooled" x="3" y="0" z="-1" />
  <Block id="hbm:watz_casing_tooled" x="3" y="0" z="0" />
  <Block id="hbm:watz_casing_tooled" x="3" y="0" z="1" />
  <Block id="hbm:watz_casing_tooled" x="3" y="1" z="-1" />
  <Block id="hbm:watz_casing_tooled" x="3" y="1" z="0" />
  <Block id="hbm:watz_casing_tooled" x="3" y="1" z="1" />
  <Block id="hbm:watz_casing_tooled" x="3" y="2" z="-1" />
  <Block id="hbm:watz_casing_tooled" x="3" y="2" z="0" />
  <Block id="hbm:watz_casing_tooled" x="3" y="2" z="1" />
</GameScene>

[Реакторный участок](./reactors.md) · [Каталог](./catalog-nuclear.md)
