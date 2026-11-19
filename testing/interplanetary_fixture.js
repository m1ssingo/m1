// Apply only to a disposable test server. Setup replaces terrain around the test pads.
ServerEvents.commandRegistry(event => {
  const api = event.commands
  const classes = {
    pos: Java.loadClass('net.minecraft.core.BlockPos'),
    id: Java.loadClass('net.minecraft.resources.ResourceLocation'),
    registries: Java.loadClass('net.minecraft.core.registries.BuiltInRegistries'),
    uuid: Java.loadClass('java.util.UUID'),
    stages: Java.loadClass('net.bananemdnsa.historystages.api.stage.StageStates'),
    craftingInput: Java.loadClass('net.minecraft.world.item.crafting.CraftingInput'),
    recipeType: Java.loadClass('net.minecraft.world.item.crafting.RecipeType'),
    list: Java.loadClass('java.util.ArrayList'),
    frequency: Java.loadClass('mekanism.common.lib.frequency.FrequencyType'),
    identity: Java.loadClass('mekanism.common.lib.frequency.Frequency$FrequencyIdentity'),
    security: Java.loadClass('mekanism.api.security.SecurityMode'),
    quests: Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile'),
    teamData: Java.loadClass('dev.ftb.mods.ftbquests.quest.TeamData'),
    fakePlayers: Java.loadClass('net.neoforged.neoforge.common.util.FakePlayerFactory')
  }
  const earth = 'minecraft:overworld'
  const moon = 'deepfield:moon'
  const stage = 'm1_interplanetary_logistics'
  const x = 8, y = 160, z = 8
  const recipes = [
    'mekanism:teleportation_core', 'mekanism:teleporter', 'mekanism:teleporter_frame',
    'mekanism:portable_teleporter', 'mekanism:quantum_entangloporter', 'mekanism:module_teleportation_unit',
    'ae2:network/blocks/quantum_ring', 'ae2:network/blocks/quantum_link'
  ]
  const levels = server => {
    const result = {}
    server.getAllLevels().forEach(level => { result[String(level.dimension)] = level })
    return result
  }
  const tile = level => level.getBlockEntity(new classes.pos(x, y, z))
  const probe = result => console.info('[M1-LINK-QA] ' + JSON.stringify(result))
  function register(name, task) {
    event.register(api.literal(name).requires(source => source.hasPermission(4)).executes(context => {
      try {
        task(context.getSource().getServer())
        return 1
      } catch (error) {
        console.error('[M1-LINK-QA] ' + String(error))
        throw error
      }
    }))
  }
  register('m1linksetup', server => {
    const worlds = levels(server)
    const state = id => classes.registries.BLOCK.get(classes.id.parse(id)).defaultBlockState()
    const floor = state('minecraft:stone')
    const air = state('minecraft:air')
    const base = state('mekanism:teleporter')
    const frame = state('mekanism:teleporter_frame')
    const owner = classes.uuid.fromString('00000000-0000-0000-0000-000000000071')
    const name = new classes.identity('m1_link_fixture', classes.security.PUBLIC, null)
    ;[earth, moon].forEach(dim => {
      const level = worlds[dim]
      level.setChunkForced(0, 0, true)
      level.getChunk(0, 0)
      const place = (px, py, pz, block) => level.setBlock(new classes.pos(px, py, pz), block, 3)
      for (let px = 4; px <= 12; px++) {
        for (let pz = 4; pz <= 12; pz++) {
          place(px, y - 1, pz, floor)
          for (let py = y; py <= y + 4; py++) place(px, py, pz, air)
        }
      }
      place(x, y, z, base)
      for (let dy = 0; dy <= 3; dy++) {
        place(x - 1, y + dy, z, frame)
        place(x + 1, y + dy, z, frame)
      }
      place(x, y + 3, z, frame)
      const terminal = tile(level)
      if (terminal === null) throw new Error('No terminal in ' + dim)
      terminal.setFrequency(classes.frequency.TELEPORTER, name, owner)
      terminal.getEnergyContainer().setEnergy(terminal.getEnergyContainer().getMaxEnergy())
    })
    probe({setup: true})
  })
  register('m1linkunlock', server => {
    const questFile = classes.quests.getInstance().orElseThrow()
    let reward = null
    questFile.getAllChapters().forEach(chapter => chapter.getQuests().forEach(quest => {
      if (String(quest.getCodeString()) === '4A70000000000102') reward = quest.getRewards().iterator().next()
    }))
    if (reward === null || !reward.isTeamReward()) throw new Error('Missing or non-team research reward')
    probe({claim: reward.claim(classes.fakePlayers.getMinecraft(server.overworld()), false)})
  })
  register('m1linkprobe', server => {
    const manager = server.getRecipeManager()
    const worlds = levels(server)
    const listed = new Set()
    manager.getRecipes().forEach(recipe => listed.add(String(recipe.id())))
    const core = manager.byKey(classes.id.parse(recipes[0])).orElseThrow().value()
    const ingredients = core.getIngredients()
    const grid = new classes.list()
    ;['minecraft:ender_pearl', 'deepfield:titanium_plate', 'minecraft:ender_pearl',
      'deepfield:moonglass_shard', 'mekanism:ultimate_control_circuit', 'deepfield:moonglass_shard',
      'minecraft:ender_pearl', 'deepfield:titanium_plate', 'minecraft:ender_pearl'].forEach(id => grid.add(Item.of(id)))
    const matched = manager.getRecipeFor(classes.recipeType.CRAFTING,
      classes.craftingInput.of(3, 3, grid), server.overworld())
    const questFile = classes.quests.getInstance().orElseThrow()
    const ids = ['4A70000000000108', '4A70000000000109']
    let checks = []
    questFile.getAllChapters().forEach(chapter => chapter.getQuests().forEach(quest => {
      if (ids.indexOf(String(quest.getCodeString())) >= 0) checks.push(quest)
    }))
    const players = []
    server.getPlayerList().getPlayers().forEach(player => {
      const routes = ['earth_to_moon', 'moon_to_earth'].map(route => {
        const advancement = server.getAdvancements().get(classes.id.parse('m1prototype:logistics/' + route))
        return advancement !== null && player.getAdvancements().getOrStartProgress(advancement).isDone()
      })
      const team = classes.teamData.get(player)
      players.push({dimension: String(player.level.dimension), routes: routes,
        done: checks.map(quest => team.isCompleted(quest))})
    })
    probe({
      unlocked: classes.stages.isUnlockedGlobal(stage, server.overworld()),
      listed: recipes.map(recipe => listed.has(recipe)),
      retained: recipes.map(recipe => manager.byKey(classes.id.parse(recipe)).isPresent()),
      core_craftable: matched.isPresent() && String(matched.get().id()) === recipes[0],
      core: ['deepfield:titanium_plate', 'deepfield:moonglass_shard', 'mekanism:ultimate_control_circuit']
        .map(id => ingredients.stream().filter(ingredient => ingredient.test(Item.of(id))).count()),
      terminals: [earth, moon].map(dim => {
        const endpoint = tile(worlds[dim])
        return endpoint === null ? null : {status: String(endpoint.status), energy: endpoint.getEnergyContainer().getEnergy()}
      }),
      players: players
    })
  })
})
