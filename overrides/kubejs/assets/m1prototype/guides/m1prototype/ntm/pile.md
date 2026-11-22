---
navigation:
  title: Чикагская поленница
  icon: hbm:pile_brick
  parent: reactors.md
  position: 3
item_ids:
  - hbm:pile_brick
  - hbm:pile_loader
  - hbm:pile_control
  - hbm:pile_vent
  - hbm:hand_drill
---

# Чикагская поленница

Поленница обрабатывает игровые топливные стержни. Кладка из графитовых блоков собирается вручную, а каналы сверлятся после формирования корпуса.

## Кладка и сверление

1. Сложи **125 графитовых блоков Чикагской поленницы** в сплошной объём 5×5×5. Построй пять одинаковых слоёв по 25 блоков. Внутренние клетки тоже заняты.
2. Ручной дрелью нажми на средний блок выбранной передней грани. Для северной грани примера это клетка `(2, 2, 0)`, если северо-западный нижний угол задан как `(0, 0, 0)`. Там появится панель.
3. Просверли топливный канал вдоль оси панели и поставь загрузчик на его входе. Пример отверстия на той же северной грани — `(1, 2, 0)`.
4. Для охлаждения просверли воздуховод поперёк топливного канала. В примере с запада это `(0, 1, 2)`, то есть на блок ниже топлива. Сверху можно добавить вертикальный канал управления. Проверяй, чтобы оси уже просверленных каналов не пересекались.
5. Закрой лишнее отверстие повторным нажатием дрелью по его входу. Вставь вентиль в нужный канал и подай **сжатый воздух 1 PU** напрямую от компрессора.

<GameScene zoom="1.7" interactive={true}>
  <IsometricCamera yaw="135" pitch="34" />
  <Block id="hbm:pile_brick" x="0" y="0" z="0" />
  <Block id="hbm:pile_brick" x="1" y="0" z="0" />
  <Block id="hbm:pile_brick" x="2" y="0" z="0" />
  <Block id="hbm:pile_brick" x="3" y="0" z="0" />
  <Block id="hbm:pile_brick" x="4" y="0" z="0" />
  <Block id="hbm:pile_brick" x="0" y="0" z="1" />
  <Block id="hbm:pile_brick" x="1" y="0" z="1" />
  <Block id="hbm:pile_brick" x="2" y="0" z="1" />
  <Block id="hbm:pile_brick" x="3" y="0" z="1" />
  <Block id="hbm:pile_brick" x="4" y="0" z="1" />
  <Block id="hbm:pile_brick" x="0" y="0" z="2" />
  <Block id="hbm:pile_brick" x="1" y="0" z="2" />
  <Block id="hbm:pile_brick" x="2" y="0" z="2" />
  <Block id="hbm:pile_brick" x="3" y="0" z="2" />
  <Block id="hbm:pile_brick" x="4" y="0" z="2" />
  <Block id="hbm:pile_brick" x="0" y="0" z="3" />
  <Block id="hbm:pile_brick" x="1" y="0" z="3" />
  <Block id="hbm:pile_brick" x="2" y="0" z="3" />
  <Block id="hbm:pile_brick" x="3" y="0" z="3" />
  <Block id="hbm:pile_brick" x="4" y="0" z="3" />
  <Block id="hbm:pile_brick" x="0" y="0" z="4" />
  <Block id="hbm:pile_brick" x="1" y="0" z="4" />
  <Block id="hbm:pile_brick" x="2" y="0" z="4" />
  <Block id="hbm:pile_brick" x="3" y="0" z="4" />
  <Block id="hbm:pile_brick" x="4" y="0" z="4" />
  <Block id="hbm:pile_brick" x="0" y="1" z="0" />
  <Block id="hbm:pile_brick" x="1" y="1" z="0" />
  <Block id="hbm:pile_brick" x="2" y="1" z="0" />
  <Block id="hbm:pile_brick" x="3" y="1" z="0" />
  <Block id="hbm:pile_brick" x="4" y="1" z="0" />
  <Block id="hbm:pile_brick" x="0" y="1" z="1" />
  <Block id="hbm:pile_brick" x="1" y="1" z="1" />
  <Block id="hbm:pile_brick" x="2" y="1" z="1" />
  <Block id="hbm:pile_brick" x="3" y="1" z="1" />
  <Block id="hbm:pile_brick" x="4" y="1" z="1" />
  <Block id="hbm:pile_brick" x="0" y="1" z="2" />
  <Block id="hbm:pile_brick" x="1" y="1" z="2" />
  <Block id="hbm:pile_brick" x="2" y="1" z="2" />
  <Block id="hbm:pile_brick" x="3" y="1" z="2" />
  <Block id="hbm:pile_brick" x="4" y="1" z="2" />
  <Block id="hbm:pile_brick" x="0" y="1" z="3" />
  <Block id="hbm:pile_brick" x="1" y="1" z="3" />
  <Block id="hbm:pile_brick" x="2" y="1" z="3" />
  <Block id="hbm:pile_brick" x="3" y="1" z="3" />
  <Block id="hbm:pile_brick" x="4" y="1" z="3" />
  <Block id="hbm:pile_brick" x="0" y="1" z="4" />
  <Block id="hbm:pile_brick" x="1" y="1" z="4" />
  <Block id="hbm:pile_brick" x="2" y="1" z="4" />
  <Block id="hbm:pile_brick" x="3" y="1" z="4" />
  <Block id="hbm:pile_brick" x="4" y="1" z="4" />
  <Block id="hbm:pile_brick" x="0" y="2" z="0" />
  <Block id="hbm:pile_brick" x="1" y="2" z="0" />
  <Block id="hbm:pile_brick" x="2" y="2" z="0" />
  <Block id="hbm:pile_brick" x="3" y="2" z="0" />
  <Block id="hbm:pile_brick" x="4" y="2" z="0" />
  <Block id="hbm:pile_brick" x="0" y="2" z="1" />
  <Block id="hbm:pile_brick" x="1" y="2" z="1" />
  <Block id="hbm:pile_brick" x="2" y="2" z="1" />
  <Block id="hbm:pile_brick" x="3" y="2" z="1" />
  <Block id="hbm:pile_brick" x="4" y="2" z="1" />
  <Block id="hbm:pile_brick" x="0" y="2" z="2" />
  <Block id="hbm:pile_brick" x="1" y="2" z="2" />
  <Block id="hbm:pile_brick" x="2" y="2" z="2" />
  <Block id="hbm:pile_brick" x="3" y="2" z="2" />
  <Block id="hbm:pile_brick" x="4" y="2" z="2" />
  <Block id="hbm:pile_brick" x="0" y="2" z="3" />
  <Block id="hbm:pile_brick" x="1" y="2" z="3" />
  <Block id="hbm:pile_brick" x="2" y="2" z="3" />
  <Block id="hbm:pile_brick" x="3" y="2" z="3" />
  <Block id="hbm:pile_brick" x="4" y="2" z="3" />
  <Block id="hbm:pile_brick" x="0" y="2" z="4" />
  <Block id="hbm:pile_brick" x="1" y="2" z="4" />
  <Block id="hbm:pile_brick" x="2" y="2" z="4" />
  <Block id="hbm:pile_brick" x="3" y="2" z="4" />
  <Block id="hbm:pile_brick" x="4" y="2" z="4" />
  <Block id="hbm:pile_brick" x="0" y="3" z="0" />
  <Block id="hbm:pile_brick" x="1" y="3" z="0" />
  <Block id="hbm:pile_brick" x="2" y="3" z="0" />
  <Block id="hbm:pile_brick" x="3" y="3" z="0" />
  <Block id="hbm:pile_brick" x="4" y="3" z="0" />
  <Block id="hbm:pile_brick" x="0" y="3" z="1" />
  <Block id="hbm:pile_brick" x="1" y="3" z="1" />
  <Block id="hbm:pile_brick" x="2" y="3" z="1" />
  <Block id="hbm:pile_brick" x="3" y="3" z="1" />
  <Block id="hbm:pile_brick" x="4" y="3" z="1" />
  <Block id="hbm:pile_brick" x="0" y="3" z="2" />
  <Block id="hbm:pile_brick" x="1" y="3" z="2" />
  <Block id="hbm:pile_brick" x="2" y="3" z="2" />
  <Block id="hbm:pile_brick" x="3" y="3" z="2" />
  <Block id="hbm:pile_brick" x="4" y="3" z="2" />
  <Block id="hbm:pile_brick" x="0" y="3" z="3" />
  <Block id="hbm:pile_brick" x="1" y="3" z="3" />
  <Block id="hbm:pile_brick" x="2" y="3" z="3" />
  <Block id="hbm:pile_brick" x="3" y="3" z="3" />
  <Block id="hbm:pile_brick" x="4" y="3" z="3" />
  <Block id="hbm:pile_brick" x="0" y="3" z="4" />
  <Block id="hbm:pile_brick" x="1" y="3" z="4" />
  <Block id="hbm:pile_brick" x="2" y="3" z="4" />
  <Block id="hbm:pile_brick" x="3" y="3" z="4" />
  <Block id="hbm:pile_brick" x="4" y="3" z="4" />
  <Block id="hbm:pile_brick" x="0" y="4" z="0" />
  <Block id="hbm:pile_brick" x="1" y="4" z="0" />
  <Block id="hbm:pile_brick" x="2" y="4" z="0" />
  <Block id="hbm:pile_brick" x="3" y="4" z="0" />
  <Block id="hbm:pile_brick" x="4" y="4" z="0" />
  <Block id="hbm:pile_brick" x="0" y="4" z="1" />
  <Block id="hbm:pile_brick" x="1" y="4" z="1" />
  <Block id="hbm:pile_brick" x="2" y="4" z="1" />
  <Block id="hbm:pile_brick" x="3" y="4" z="1" />
  <Block id="hbm:pile_brick" x="4" y="4" z="1" />
  <Block id="hbm:pile_brick" x="0" y="4" z="2" />
  <Block id="hbm:pile_brick" x="1" y="4" z="2" />
  <Block id="hbm:pile_brick" x="2" y="4" z="2" />
  <Block id="hbm:pile_brick" x="3" y="4" z="2" />
  <Block id="hbm:pile_brick" x="4" y="4" z="2" />
  <Block id="hbm:pile_brick" x="0" y="4" z="3" />
  <Block id="hbm:pile_brick" x="1" y="4" z="3" />
  <Block id="hbm:pile_brick" x="2" y="4" z="3" />
  <Block id="hbm:pile_brick" x="3" y="4" z="3" />
  <Block id="hbm:pile_brick" x="4" y="4" z="3" />
  <Block id="hbm:pile_brick" x="0" y="4" z="4" />
  <Block id="hbm:pile_brick" x="1" y="4" z="4" />
  <Block id="hbm:pile_brick" x="2" y="4" z="4" />
  <Block id="hbm:pile_brick" x="3" y="4" z="4" />
  <Block id="hbm:pile_brick" x="4" y="4" z="4" />
  <BlockAnnotation x="2" y="2" z="0" color="#A1D071">Точка формирования корпуса на северной грани.</BlockAnnotation>
  <BlockAnnotation x="1" y="2" z="0" color="#E6BB61">Пример входа топливного канала.</BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="2" color="#4C9EEF">Пример входа воздуховода на западной грани.</BlockAnnotation>
</GameScene>

## Перед загрузкой

На пустом корпусе проверь доступ к панели и загрузчику, затем запусти компрессор и проверь вентиляцию. Регулирующие стержни запускаются отдельно. Сигнал редстоуна на их устройстве **извлекает** стержни. После остановки оставляй вентиляцию работать до снижения температуры.

Пример координат показывает построение корпуса и назначение отверстий. Количество каналов, охлаждение и состав топлива для действующей установки подбираются по рабочему проекту.
