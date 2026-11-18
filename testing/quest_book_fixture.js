// Only installed in the disposable server while the migration probe is running.
function exerciseQuestBookFixture(context, mode) {
  try {
    var File = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile')
    var ItemTask = Java.loadClass('dev.ftb.mods.ftbquests.quest.task.ItemTask')
    var UUID = Java.loadClass('java.util.UUID')
    var file = File.getInstance().orElseThrow()
    var fullId = UUID.fromString('00000000-2320-4000-8000-000000000001')
    var midId = UUID.fromString('00000000-2320-4000-8000-000000000002')
    var quests = {}
    var counts = []
    var optional = 0
    var items = 0
    file.getAllChapters().forEach(chapter => {
      counts.push(chapter.getQuests().size())
      chapter.getQuests().forEach(quest => {
        quests[String(quest.getCodeString())] = quest
        if (quest.isOptional()) optional++
        quest.getTasksAsList().forEach(task => {
          if (task instanceof ItemTask) {
            if (task.getItemStack().isEmpty()) throw new Error('Missing item at ' + quest.getCodeString())
            items++
          }
        })
      })
    })
    var findQuest = (chapter, index) => {
      var id = '4A' + chapter + '00000000001' + (index < 16 ? '0' : '') + index.toString(16).toUpperCase()
      if (!quests[id]) throw new Error('Missing quest ' + id)
      return quests[id]
    }
    var expect = (ok, label) => {
      if (!ok) throw new Error(label)
    }
    var finish = (data, node) => {
      expect(data.areDependenciesComplete(node), 'Blocked quest ' + node.getCodeString())
      node.getTasksAsList().forEach(task => data.setProgress(task, task.getMaxProgress()))
      expect(data.isCompleted(node), 'Did not complete ' + node.getCodeString())
    }
    var electronicsReward = findQuest(1, 6).getRewards().iterator().next()
    if (mode === 'seed') {
      expect(counts.length === 2 && optional === 0, 'Start migration test from 0.3.1')
      var full = file.getOrCreateTeamData(fullId)
      var mid = file.getOrCreateTeamData(midId)
      for (var i = 1; i <= 9; i++) {
        finish(full, findQuest(1, i))
        finish(mid, findQuest(1, i))
      }
      for (var i = 1; i <= 6; i++) finish(full, findQuest(2, i))
      finish(mid, findQuest(2, 1))
      full.markRewardAsClaimed(fullId, electronicsReward, 1760000000000)
      full.saveIfChanged()
      mid.saveIfChanged()
      console.info('[M1-QUEST-BOOK] ' + JSON.stringify({mode: mode, counts: counts, teams_saved: 2}))
      return 1
    }
    var full = file.getNullableTeamData(fullId)
    var mid = file.getNullableTeamData(midId)
    expect(full && mid, 'Saved quest data did not load')
    expect(full.getRewardClaimTime(fullId, electronicsReward).isPresent(), 'Reward claim did not survive')
    for (var i = 1; i <= 9; i++) expect(full.isCompleted(findQuest(1, i)), 'Old first chapter progress lost')
    for (var i = 1; i <= 6; i++) expect(full.isCompleted(findQuest(2, i)), 'Old chemistry progress lost')
    var ntmStart = findQuest(3, 1)
    var commissioning = findQuest(3, 8)
    expect(full.areDependenciesComplete(ntmStart), 'Established team cannot start next chapter')
    expect(!mid.areDependenciesComplete(ntmStart), 'Midway team started next chapter early')
    expect(mid.areDependenciesComplete(findQuest(2, 2)) && mid.areDependenciesComplete(findQuest(2, 3)),
      'Ore processing and electronics must both start after chemistry entry')
    for (var i = 3; i <= 5; i++) finish(mid, findQuest(2, i))
    expect(!mid.areDependenciesComplete(findQuest(2, 6)), 'Chemistry exit ignored ore processing')
    finish(mid, findQuest(2, 2))
    finish(mid, findQuest(2, 6))
    for (var i = 1; i < 8; i++) finish(mid, findQuest(3, i))
    finish(mid, commissioning)
    Object.keys(quests).forEach(id => {
      var node = quests[id]
      if (node.isOptional()) expect(!mid.isCompleted(node), 'Optional objective should stay open')
    })
    var Anvils = Java.loadClass('com.hbm.inventory.recipes.anvil.AnvilRecipes')
    var circuit = Item.of('mekanism:advanced_control_circuit', 2)
    var bootstrap = Anvils.getConstruction().stream()
      .filter(recipe => recipe.isTierValid(2) &&
        String(recipe.getDisplay().getItem()) === 'hbm:machine_assembly_machine').toList()
    expect(bootstrap.size() === 1, 'Expected one active assembler bootstrap')
    var circuitInputs = bootstrap.get(0).input.stream()
      .filter(ingredient => ingredient.matchesRecipe(circuit, false)).toList()
    expect(circuitInputs.size() === 1 && circuitInputs.get(0).stacksize === 2,
      'NTM anvil must request two advanced Mekanism circuits')
    console.info('[M1-QUEST-BOOK] ' + JSON.stringify({
      mode: mode, counts: counts, optional: optional, resolved_item_tasks: items, old_progress_preserved: true,
      claimed_reward_preserved: true, chemistry_parallel: true, ntm_requires_chemistry: true,
      new_chapter_complete_without_optional: true, assembler_advanced_circuits: 2
    }))
    return 1
  } catch (error) {
    console.error('[M1-QUEST-BOOK] ' + JSON.stringify({error: String(error), stack: String(error.stack)}))
    throw error
  }
}

ServerEvents.commandRegistry(event => {
  event.register(event.commands.literal('m1questbook')
    .requires(source => source.hasPermission(4))
    .then(event.commands.literal('seed').executes(context => exerciseQuestBookFixture(context, 'seed')))
    .then(event.commands.literal('verify').executes(context => exerciseQuestBookFixture(context, 'verify'))))
})
