---
navigation:
  title: ВВЭР
  icon: hbm:pwr_controller
  parent: reactors.md
  position: 6
item_ids:
  - hbm:pwr_controller
  - hbm:pwr_port
  - hbm:pwr_casing
  - hbm:pwr_heatex
---

# ВВЭР

Корпус ВВЭР собирается из отдельных блоков. Теплоноситель забирает тепло у топливного участка и дальше поступает на теплообмен. Здесь показана сборка для холодной проверки контроллера и люка доступа.

## Корпус 3×4 в плане, высота 3 блока

Ставь блоки от нижнего северо-западного угла `(0, 0, 0)`. X растёт на восток, Z на юг.

<GameScene zoom="2.4" interactive={true}>
  <IsometricCamera yaw="135" pitch="34" />
  <Block id="hbm:pwr_casing" x="0" y="0" z="0" />
  <Block id="hbm:pwr_casing" x="1" y="0" z="0" />
  <Block id="hbm:pwr_casing" x="2" y="0" z="0" />
  <Block id="hbm:pwr_casing" x="0" y="0" z="1" />
  <Block id="hbm:pwr_casing" x="1" y="0" z="1" />
  <Block id="hbm:pwr_casing" x="2" y="0" z="1" />
  <Block id="hbm:pwr_casing" x="0" y="0" z="2" />
  <Block id="hbm:pwr_casing" x="1" y="0" z="2" />
  <Block id="hbm:pwr_casing" x="2" y="0" z="2" />
  <Block id="hbm:pwr_casing" x="0" y="0" z="3" />
  <Block id="hbm:pwr_casing" x="1" y="0" z="3" />
  <Block id="hbm:pwr_casing" x="2" y="0" z="3" />
  <Block id="hbm:pwr_casing" x="0" y="1" z="0" />
  <Block id="hbm:pwr_controller" x="1" y="1" z="0" p:facing="north" />
  <Block id="hbm:pwr_casing" x="2" y="1" z="0" />
  <Block id="hbm:pwr_casing" x="0" y="1" z="1" />
  <Block id="hbm:pwr_fuel" x="1" y="1" z="1" />
  <Block id="hbm:pwr_casing" x="2" y="1" z="1" />
  <Block id="hbm:pwr_casing" x="0" y="1" z="2" />
  <Block id="hbm:pwr_neutron_source" x="1" y="1" z="2" />
  <Block id="hbm:pwr_casing" x="2" y="1" z="2" />
  <Block id="hbm:pwr_casing" x="0" y="1" z="3" />
  <Block id="hbm:pwr_port" x="1" y="1" z="3" />
  <Block id="hbm:pwr_casing" x="2" y="1" z="3" />
  <Block id="hbm:pwr_casing" x="0" y="2" z="0" />
  <Block id="hbm:pwr_casing" x="1" y="2" z="0" />
  <Block id="hbm:pwr_casing" x="2" y="2" z="0" />
  <Block id="hbm:pwr_casing" x="0" y="2" z="1" />
  <Block id="hbm:pwr_casing" x="1" y="2" z="1" />
  <Block id="hbm:pwr_casing" x="2" y="2" z="1" />
  <Block id="hbm:pwr_casing" x="0" y="2" z="2" />
  <Block id="hbm:pwr_casing" x="1" y="2" z="2" />
  <Block id="hbm:pwr_casing" x="2" y="2" z="2" />
  <Block id="hbm:pwr_casing" x="0" y="2" z="3" />
  <Block id="hbm:pwr_casing" x="1" y="2" z="3" />
  <Block id="hbm:pwr_casing" x="2" y="2" z="3" />
  <BlockAnnotation x="1" y="1" z="0" color="#8EC47F">Контроллер смотрит на север.</BlockAnnotation>
  <BlockAnnotation x="1" y="1" z="3" color="#69A6DB">Люк доступа в южной стенке.</BlockAnnotation>
</GameScene>

1. Сделай нижний и верхний слои 3×4 из корпуса ВВЭР. В среднем слое положи два ряда корпуса по краям, на X = 0 и X = 2.
2. В среднем ряду на X = 1 поставь по порядку от севера к югу **Контроллер (ВВЭР)**, **Топливный стержень (ВВЭР)**, **Источник нейтронов** и **Люк доступа (ВВЭР)**. Топливный блок при монтаже остаётся пустым.
3. Открой контроллер с северной стороны. В игре он проверяет соседние блоки за панелью и формирует реактор. Если сборка не происходит, проверь направление контроллера. Внутри должны быть топливный канал и источник, а вокруг — замкнутая оболочка.

В испытании этот корпус сформировался и принял все три блока за контроллером. Для выработки тепла нужны совместимое топливо, размещение блоков теплообмена и подключение контура по параметрам рабочего проекта. Перед вводом в работу проверь направление потока на пустой установке. При остановке продолжай отводить тепло и проверь, что показания снижаются. Топливо выгружай после остывания.

[Топливо и отходы](./nuclear-materials.md) · [Каталог реакторного оборудования](./catalog-nuclear.md)
