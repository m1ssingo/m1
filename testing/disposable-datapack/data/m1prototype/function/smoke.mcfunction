# Disposable test fixture only; never install in an existing play world.
data modify storage m1prototype:checks format set value 1
setblock 0 100 0 minecraft:air
setblock 0 100 0 minecraft:chest
execute store success storage m1prototype:checks mekanism_circuit byte 1 run item replace block 0 100 0 container.0 with mekanism:basic_control_circuit
execute store success storage m1prototype:checks ae2_controller_item byte 1 run item replace block 0 100 0 container.1 with ae2:controller
execute store success storage m1prototype:checks appmek_cell byte 1 run item replace block 0 100 0 container.2 with appmek:chemical_storage_cell_1k
execute store success storage m1prototype:checks deepfield_launch_kit byte 1 run item replace block 0 100 0 container.3 with deepfield:launch_kit
execute store success storage m1prototype:checks openfm_item byte 1 run item replace block 0 100 0 container.4 with openfm:radio_block
setblock 2 100 0 minecraft:air
execute store success storage m1prototype:checks mekanism_energy_cube byte 1 run setblock 2 100 0 mekanism:basic_energy_cube
setblock 4 100 0 minecraft:air
execute store success storage m1prototype:checks ae2_energy_acceptor byte 1 run setblock 4 100 0 ae2:energy_acceptor
setblock 5 100 0 minecraft:air
execute store success storage m1prototype:checks ae2_energy_cell byte 1 run setblock 5 100 0 ae2:energy_cell
setblock 7 100 0 minecraft:air
execute store success storage m1prototype:checks hbm_battery byte 1 run setblock 7 100 0 hbm:machine_battery
setblock 9 100 0 minecraft:air
execute store success storage m1prototype:checks openfm_radio byte 1 run setblock 9 100 0 openfm:radio_block
setblock 11 100 0 minecraft:air
execute store success storage m1prototype:checks deepfield_oxygen_generator byte 1 run setblock 11 100 0 deepfield:oxygen_generator
execute in deepfield:moon run forceload remove 0 0
execute store success storage m1prototype:checks moon_dimension byte 1 run execute in deepfield:moon run forceload add 0 0
data get storage m1prototype:checks
