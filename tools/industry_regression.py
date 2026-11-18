"""Exercise real Mekanism block entities on a disposable server world."""

import asyncio
import json
import re
from pathlib import Path

STAGE = 'm1_proto_electronics'
STORAGE = 'm1industry:verification'
FIXTURE = Path(__file__).resolve().parents[1] / 'testing/industry_fixture.js'


async def exercise_industry(command, wait_for):
    async def stage(action):
        await command(f'history global {action} {STAGE}')
        await command('m1probe')
        state = 'true' if action == 'unlock' else 'false'
        await wait_for(f'"stage_unlocked":{state}')

    async def step(ticks):
        async def game_time():
            await command('time query gametime')
            line = await wait_for('The time is ')
            return int(re.search(r'The time is (\d+)', line).group(1))
        started = await game_time()
        await command(f'tick step {ticks}')
        await wait_for(f'Stepping {ticks} tick(s)')
        # Reloads can pause the tick loop for seconds; wall time is not a tick counter.
        async with asyncio.timeout(40):
            while await game_time() - started < ticks:
                await asyncio.sleep(0.5)

    async def machine(x, name, slots):
        await command(f'setblock {x} 106 3 minecraft:air')
        await command(f'setblock {x - 1} 106 3 immersiveengineering:capacitor_creative')
        await command(f'setblock {x} 106 3 mekanism:{name}')
        await command(
            f'data merge block {x} 106 3 '
            f'{{component_config:{{config0:[I;1,1,1,1,1,1]}},items:[{slots}]}}'
        )

    async def check(key, x, nbt_path, *, expected=True):
        condition = 'if' if expected else 'unless'
        await command(
            f'execute store success storage {STORAGE} {key} byte 1 run '
            f'execute {condition} data block {x} 106 3 {nbt_path}'
        )

    def slot(index, item, count):
        return f'{{slot:{index}b,item:{{id:"{item}",count:{count}}}}}'

    circuit = 'mekanism:basic_control_circuit'
    osmium = 'mekanism:ingot_osmium'
    dust = 'minecraft:redstone'
    await command('forceload add 0 0')
    await command('tick rate 100')
    await command('tick freeze')
    await stage('lock')

    # Non-OP FakePlayer exercises the actual command reward, including elevation.
    await command('m1industryfixture')
    await wait_for('[M1-INDUSTRY] {')
    await command('m1probe')
    await wait_for('"stage_unlocked":true')
    await command(f'data modify storage {STORAGE} format set value 1')
    await machine(1, 'metallurgic_infuser', ','.join([slot(0, dust, 4), slot(1, osmium, 2)]))
    await machine(4, 'basic_infusing_factory', ','.join([slot(6, dust, 4), slot(0, osmium, 2)]))
    await step(45)
    await command('data get block 1 106 3 progress')
    await wait_for('1, 106, 3 has the following block data:')
    await command('data get block 4 106 3 progress')
    await wait_for('4, 106, 3 has the following block data:')
    # The exact values vary if the energy connection takes a tick to warm up.
    for key, x, path in [('infuser_was_active', 1, 'progress'), ('factory_was_active', 4, 'progress[0]')]:
        await command(
            f'execute store result storage {STORAGE} {key} int 1 run data get block {x} 106 3 {path}'
        )
    await stage('lock')
    await machine(7, 'metallurgic_infuser', ','.join([slot(0, dust, 2), slot(1, osmium, 1)]))
    await machine(10, 'metallurgic_infuser', ','.join([slot(0, dust, 2), slot(1, 'minecraft:copper_ingot', 1)]))
    # Reset neighbours too: IE recalculates this generator on a neighbour update.
    # Leaving the same blocks from an earlier run makes the fixture order-dependent.
    await command('setblock 9 106 2 minecraft:air')
    await command('setblock 9 106 4 minecraft:air')
    await command('setblock 9 106 3 immersiveengineering:thermoelectric_generator')
    await command('setblock 9 106 2 minecraft:magma_block')
    await command('setblock 9 106 4 minecraft:snow_block')
    await step(500)
    for key, x, index, remaining in [
        ('infuser', 1, 1, 2), ('factory', 4, 0, 2), ('fresh_infuser', 7, 1, 1)
    ]:
        await check(f'{key}_locked_no_circuits', x, f'items[{{item:{{id:"{circuit}"}}}}]', expected=False)
        await check(f'{key}_locked_inputs_retained', x,
                    f'items[{{slot:{index}b,item:{{id:"{osmium}",count:{remaining}}}}}]')
    await check('unrestricted_alloy_while_locked', 10, 'items[{item:{id:"mekanism:alloy_infused"}}]')
    await command('data get block 10 106 3')
    await command('data get block 9 106 3')

    await stage('unlock')
    await step(500)
    for key, x, required in [('infuser', 1, 2), ('factory', 4, 2), ('fresh_infuser', 7, 1)]:
        await check(f'{key}_works_after_unlock', x, f'items[{{item:{{id:"{circuit}",count:{required}}}}}]')
    await command(f'data get storage {STORAGE}')
    await wait_for('verification has the following contents:')
    await command('tick rate 20')
    await command('tick unfreeze')


def extract_results(log):
    chapter_lines = [line.split('[M1-INDUSTRY] ', 1)[1] for line in log.splitlines()
                     if '[M1-INDUSTRY] {' in line]
    storage_lines = [line for line in log.splitlines() if 'verification has the following contents:' in line]
    assert len(chapter_lines) == len(storage_lines) == 1, (chapter_lines, storage_lines)
    chapter = json.loads(chapter_lines[0])
    assert chapter == {
        'quests': 16, 'chemistry_quests': 15, 'separator_advanced_slots': 2, 'chapter_title': 'От угля к автоматизации',
        'chapter_final_title': 'Первый автокрафт', 'unlock_id': '4A10000000000106',
        'team_reward': True, 'reward_claimed': True, 'stage_before': False,
        'infuser_steel_slots': 1, 'acceptor_basic_slots': 1, 'controller_basic_slots': 2
    }, chapter
    stage_lines = [json.loads(line.split('[M1-PROBE] ', 1)[1]) for line in log.splitlines()
                   if '[M1-PROBE] {' in line]
    assert [probe['stage_unlocked'] for probe in stage_lines] == [False, True, False, True]
    chapter['stage_open_after_reward_reload'] = True
    machine = {key: int(value) for key, value in re.findall(r'([a-z_]+):\s*(\d+)b?\b', storage_lines[0])}
    for key in ['infuser_was_active', 'factory_was_active']:
        assert 0 < machine[key] < 200, machine
    machine_checks = [
        f'{name}_{check}' for name in ('infuser', 'factory', 'fresh_infuser')
        for check in ('locked_no_circuits', 'locked_inputs_retained', 'works_after_unlock')
    ]
    assert all(machine.get(key) == 1 for key in [*machine_checks, 'unrestricted_alloy_while_locked']), machine
    return {'chapter': chapter, 'machines': machine}
