---
navigation:
  title: Питание и трубы
  icon: hbm:red_cable
  parent: layouts.md
  position: 2
---

# Питание и трубы

## Пробная линия HE

<GameScene zoom={2.5} interactive={true}>
  <Block id="minecraft:polished_andesite" x="0" y="0" z="0" />
  <Block id="minecraft:brown_terracotta" x="0" y="0" z="1" />
  <Block id="minecraft:brown_terracotta" x="0" y="1" z="0" />
  <Block id="minecraft:brown_terracotta" x="0" y="1" z="1" />
  <Block id="minecraft:brown_terracotta" x="1" y="0" z="0" />
  <Block id="minecraft:brown_terracotta" x="1" y="0" z="1" />
  <Block id="minecraft:brown_terracotta" x="1" y="1" z="0" />
  <Block id="minecraft:brown_terracotta" x="1" y="1" z="1" />
  <Block id="minecraft:lime_terracotta" x="-1" y="0" z="5" />
  <Block id="minecraft:lime_terracotta" x="-1" y="0" z="6" />
  <Block id="minecraft:lime_terracotta" x="-1" y="0" z="7" />
  <Block id="minecraft:lime_terracotta" x="-1" y="1" z="5" />
  <Block id="minecraft:lime_terracotta" x="-1" y="1" z="6" />
  <Block id="minecraft:lime_terracotta" x="-1" y="1" z="7" />
  <Block id="minecraft:lime_terracotta" x="-1" y="2" z="5" />
  <Block id="minecraft:lime_terracotta" x="-1" y="2" z="6" />
  <Block id="minecraft:lime_terracotta" x="-1" y="2" z="7" />
  <Block id="minecraft:lime_terracotta" x="0" y="0" z="5" />
  <Block id="minecraft:polished_andesite" x="0" y="0" z="6" />
  <Block id="minecraft:lime_terracotta" x="0" y="0" z="7" />
  <Block id="minecraft:lime_terracotta" x="0" y="1" z="5" />
  <Block id="minecraft:lime_terracotta" x="0" y="1" z="6" />
  <Block id="minecraft:lime_terracotta" x="0" y="1" z="7" />
  <Block id="minecraft:lime_terracotta" x="0" y="2" z="5" />
  <Block id="minecraft:lime_terracotta" x="0" y="2" z="6" />
  <Block id="minecraft:lime_terracotta" x="0" y="2" z="7" />
  <Block id="minecraft:lime_terracotta" x="1" y="0" z="5" />
  <Block id="minecraft:lime_terracotta" x="1" y="0" z="6" />
  <Block id="minecraft:lime_terracotta" x="1" y="0" z="7" />
  <Block id="minecraft:lime_terracotta" x="1" y="1" z="5" />
  <Block id="minecraft:lime_terracotta" x="1" y="1" z="6" />
  <Block id="minecraft:lime_terracotta" x="1" y="1" z="7" />
  <Block id="minecraft:lime_terracotta" x="1" y="2" z="5" />
  <Block id="minecraft:lime_terracotta" x="1" y="2" z="6" />
  <Block id="minecraft:lime_terracotta" x="1" y="2" z="7" />
  <Block id="minecraft:orange_concrete" x="0" y="0" z="2" />
  <Block id="minecraft:orange_concrete" x="0" y="0" z="3" />
  <Block id="minecraft:orange_concrete" x="0" y="0" z="4" />
  <BlockAnnotation x="0" y="0" z="0" color="#DDA651">Поставь генератор в точке X 0, Y 0, Z 0.</BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="6" color="#8CC492">Ядро сборочной машины. Её предмет ставится в клетку Z 5.</BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="3" color="#DDA651">Три кабеля от Z 2 до Z 4.</BlockAnnotation>
</GameScene>

Коричневые клетки показывают корпус генератора, зелёные — сборочной машины, оранжевые — трассу кабеля. Цвета помогают читать расположение блоков. Материалы смотри в рецептах.

Поставь генератор в `(0, 0, 0)`, а сборочную машину в `(0, 0, 5)`. Оба корпуса смотрят на север. Проложи кабели в `(0, 0, 2)`, `(0, 0, 3)` и `(0, 0, 4)`. Включи генератор с брёвнами. Это расположение проверено на тестовом сервере.

Нагреватель ставится под приёмной частью бойлера или Стирлинга. У жидкостного участка оставь место для отдельной трубы на каждый выход. Подключай трубы к рабочим граням и сверяй их направление с интерфейсом машины.

Габариты указаны по осям X×Y×Z. Координаты ядра отсчитываются от клетки, в которой ставится предмет корпуса.

## Генератор на дровах

Предмет `hbm:machine_wood_burner`. Габарит **2×2×2** по X×Y×Z. Ядро в `(0, 0, 0)`.

## Аккумуляторный разъем

Предмет `hbm:machine_battery_socket`. Габарит **2×2×2** по X×Y×Z. Ядро в `(0, 0, 0)`.

## Генератор Стирлинга

Предмет `hbm:machine_stirling`. Габарит **3×2×3** по X×Y×Z. Ядро в `(0, 0, 1)`.

## Большой бойлер

Предмет `hbm:heat_boiler`. Габарит **3×4×3** по X×Y×Z. Ядро в `(0, 0, 1)`.

## Промышленный бойлер

Предмет `hbm:machine_industrial_boiler`. Габарит **3×5×3** по X×Y×Z. Ядро в `(0, 0, 1)`.

## Электрический насос для грунтовых вод

Предмет `hbm:machine_pump_electric`. Габарит **3×4×3** по X×Y×Z. Ядро в `(0, 0, 1)`.
