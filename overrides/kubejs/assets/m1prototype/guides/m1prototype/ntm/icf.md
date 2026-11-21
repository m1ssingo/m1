---
navigation:
  title: ICF
  icon: hbm:struct_icf_core
  parent: reactors.md
  position: 8
item_ids:
  - hbm:icf
  - hbm:struct_icf_core
---

# ICF

Реактор инерциального термоядерного синтеза использует лазерный импульс и топливную капсулу. В отдельные контуры уходят горячий жидкий натрий и звёздная материя.

## Сборка

1. Поставь **Ядро инерциального термоядерного реактора (ICF)** в `(0, 0, 0)`, лицом на север. Разложи стабилизаторы, сваренные стенки и заклёпанную обшивку на шести уровнях.
2. После установки последнего требуемого блока камера соберётся сама. Если этого не произошло, сверь вид блока у каждой клетки и поворот ядра. Для поворота установки поверни всю раскладку вокруг ядра.
3. Проверь контроллер, подведи **Жидкий натрий**. Горячий жидкий натрий и звёздную материю отведи в отдельные буферы. При зарядке и пуске ориентируйся на показания лазера и уровень теплоносителя в интерфейсе.
4. Автоподачу капсул подключай после пробной партии. При остановке прекрати подачу капсул и лазерных импульсов, оставь отвод теплоносителя и проверь выходные баки.

На сервере камера сформировалась из деталей этой карты. Карта рассчитана на монтаж и холодную проверку.

## Детали для камеры

| Метка | Блок | Количество |
| --- | --- | ---: |
| `@` | Ядро инерциального термоядерного реактора (ICF) | 1 |
| `А` | Стабилизатор (ICF) | 50 |
| `Б` | Сваренная стенка активной зоны (ICF) | 117 |
| `В` | Заклёпанная обшивка (ICF) | 240 |

<GameScene zoom="1.7" interactive={true}>
  <IsometricCamera yaw="135" pitch="35" />
  <Block id="hbm:icf_component" x="-8" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-8" y="0" z="0" />
  <Block id="hbm:icf_component" x="-8" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-8" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-8" y="5" z="1" />
  <Block id="hbm:icf_component" x="-7" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-7" y="0" z="0" />
  <Block id="hbm:icf_component" x="-7" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-7" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-7" y="5" z="1" />
  <Block id="hbm:icf_component" x="-6" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-6" y="0" z="0" />
  <Block id="hbm:icf_component" x="-6" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-6" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-6" y="5" z="1" />
  <Block id="hbm:icf_component" x="-5" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-5" y="0" z="0" />
  <Block id="hbm:icf_component" x="-5" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-5" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-5" y="5" z="1" />
  <Block id="hbm:icf_component" x="-4" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-4" y="0" z="0" />
  <Block id="hbm:icf_component" x="-4" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-4" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-4" y="5" z="1" />
  <Block id="hbm:icf_component" x="-3" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-3" y="0" z="0" />
  <Block id="hbm:icf_component" x="-3" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-3" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="-3" y="5" z="1" />
  <Block id="hbm:icf_component" x="-2" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-2" y="0" z="0" />
  <Block id="hbm:icf_component" x="-2" y="0" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="1" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="1" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="1" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="2" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="2" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="2" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="2" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="2" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="3" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="3" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="3" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="3" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="4" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="4" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="4" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="4" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="4" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="5" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="5" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-2" y="5" z="1" />
  <Block id="hbm:icf_component" x="-1" y="0" z="-1" />
  <Block id="hbm:icf_component" x="-1" y="0" z="0" />
  <Block id="hbm:icf_component" x="-1" y="0" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="1" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="1" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="1" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="2" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="2" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="2" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="2" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="2" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="3" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="3" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="3" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="3" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="4" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="4" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="4" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="4" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="4" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="5" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="5" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="-1" y="5" z="1" />
  <Block id="hbm:icf_component" x="0" y="0" z="-1" />
  <Block id="hbm:struct_icf_core" x="0" y="0" z="0" p:facing="north" />
  <Block id="hbm:icf_component" x="0" y="0" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="1" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="1" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="1" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="2" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="2" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="2" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="2" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="2" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="3" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="3" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="3" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="3" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="4" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="4" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="4" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="4" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="4" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="5" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="5" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="0" y="5" z="1" />
  <Block id="hbm:icf_component" x="1" y="0" z="-1" />
  <Block id="hbm:icf_component" x="1" y="0" z="0" />
  <Block id="hbm:icf_component" x="1" y="0" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="1" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="1" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="1" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="2" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="2" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="2" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="2" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="2" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="3" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="3" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="3" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="3" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="4" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="4" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="4" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="4" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="4" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="5" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="5" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="1" y="5" z="1" />
  <Block id="hbm:icf_component" x="2" y="0" z="-1" />
  <Block id="hbm:icf_component" x="2" y="0" z="0" />
  <Block id="hbm:icf_component" x="2" y="0" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="1" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="1" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="1" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="2" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="2" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="2" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="2" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="2" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="3" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="3" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="3" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="3" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="4" z="-2" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="4" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="4" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="4" z="1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="4" z="2" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="5" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="5" z="0" />
  <Block id="hbm:icf_component_vessel_welded" x="2" y="5" z="1" />
  <Block id="hbm:icf_component" x="3" y="0" z="-1" />
  <Block id="hbm:icf_component" x="3" y="0" z="0" />
  <Block id="hbm:icf_component" x="3" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="3" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="3" y="5" z="1" />
  <Block id="hbm:icf_component" x="4" y="0" z="-1" />
  <Block id="hbm:icf_component" x="4" y="0" z="0" />
  <Block id="hbm:icf_component" x="4" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="4" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="4" y="5" z="1" />
  <Block id="hbm:icf_component" x="5" y="0" z="-1" />
  <Block id="hbm:icf_component" x="5" y="0" z="0" />
  <Block id="hbm:icf_component" x="5" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="5" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="5" y="5" z="1" />
  <Block id="hbm:icf_component" x="6" y="0" z="-1" />
  <Block id="hbm:icf_component" x="6" y="0" z="0" />
  <Block id="hbm:icf_component" x="6" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="6" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="6" y="5" z="1" />
  <Block id="hbm:icf_component" x="7" y="0" z="-1" />
  <Block id="hbm:icf_component" x="7" y="0" z="0" />
  <Block id="hbm:icf_component" x="7" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="7" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="7" y="5" z="1" />
  <Block id="hbm:icf_component" x="8" y="0" z="-1" />
  <Block id="hbm:icf_component" x="8" y="0" z="0" />
  <Block id="hbm:icf_component" x="8" y="0" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="1" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="1" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="1" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="2" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="2" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="2" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="2" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="2" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="3" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="3" z="-1" />
  <Block id="hbm:icf_component_vessel_welded" x="8" y="3" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="3" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="3" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="4" z="-2" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="4" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="4" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="4" z="1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="4" z="2" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="5" z="-1" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="5" z="0" />
  <Block id="hbm:icf_component_structure_bolted" x="8" y="5" z="1" />
</GameScene>

## Слои от ядра

X растёт на восток, Z на юг. Диапазон включает обе крайние клетки. Пустые позиции в строке оставляй свободными.

### Y = 0

| Z | `@` | `А` | `Б` | `В` |
| --- | --- | --- | --- | --- |
| -1 | — | -8…8 | — | — |
| 0 | 0 | -8…-1, 1…8 | — | — |
| 1 | — | -8…8 | — | — |

### Y = 1

| Z | `@` | `А` | `Б` | `В` |
| --- | --- | --- | --- | --- |
| -1 | — | — | -2…2 | -8…-3, 3…8 |
| 0 | — | — | -2…2 | -8…-3, 3…8 |
| 1 | — | — | -2…2 | -8…-3, 3…8 |

### Y = 2

| Z | `@` | `А` | `Б` | `В` |
| --- | --- | --- | --- | --- |
| -2 | — | — | -2…2 | -8…-3, 3…8 |
| -1 | — | — | -2…2 | -8…-3, 3…8 |
| 0 | — | — | -2…2 | -8…-3, 3…8 |
| 1 | — | — | -2…2 | -8…-3, 3…8 |
| 2 | — | — | -2…2 | -8…-3, 3…8 |

### Y = 3

| Z | `@` | `А` | `Б` | `В` |
| --- | --- | --- | --- | --- |
| -2 | — | — | -2…2 | -8…-3, 3…8 |
| -1 | — | — | -2…2 | -8…-3, 3…8 |
| 0 | — | — | -8…8 | — |
| 1 | — | — | -2…2 | -8…-3, 3…8 |
| 2 | — | — | -2…2 | -8…-3, 3…8 |

### Y = 4

| Z | `@` | `А` | `Б` | `В` |
| --- | --- | --- | --- | --- |
| -2 | — | — | -2…2 | -8…-3, 3…8 |
| -1 | — | — | -2…2 | -8…-3, 3…8 |
| 0 | — | — | -2…2 | -8…-3, 3…8 |
| 1 | — | — | -2…2 | -8…-3, 3…8 |
| 2 | — | — | -2…2 | -8…-3, 3…8 |

### Y = 5

| Z | `@` | `А` | `Б` | `В` |
| --- | --- | --- | --- | --- |
| -1 | — | — | -2…2 | -8…-3, 3…8 |
| 0 | — | — | -2…2 | -8…-3, 3…8 |
| 1 | — | — | -2…2 | -8…-3, 3…8 |

[Реакторный участок](./reactors.md) · [Каталог](./catalog-nuclear.md)
