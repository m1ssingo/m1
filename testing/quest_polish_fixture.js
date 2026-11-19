// Copy this fixture to a disposable server's kubejs/server_scripts before running it.
ServerEvents.commandRegistry(event => {
  event.register(event.commands.literal('m1questpolishfixture')
    .requires(source => source.hasPermission(4))
    .executes(context => {
      const QuestFile = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile')
      const ItemTask = Java.loadClass('dev.ftb.mods.ftbquests.quest.task.ItemTask')
      const ItemReward = Java.loadClass('dev.ftb.mods.ftbquests.quest.reward.ItemReward')
      const CommandReward = Java.loadClass('dev.ftb.mods.ftbquests.quest.reward.CommandReward')
      const dependencies = {}
      const nodes = {}
      const chapterSizes = [25, 24, 21, 20, 22, 23, 25, 27]
      const books = new Set()
      const gates = []
      let optional = 0

      function require(condition, reason) {
        if (!condition) throw new Error(reason)
      }

      const chapters = QuestFile.getInstance().orElseThrow().getAllChapters()
      require(chapters.size() === chapterSizes.length, 'All eight chapters must load')
      chapters.forEach(chapter => {
        const code = String(chapter.getCodeString())
        const chapterIndex = Number(code.charAt(2)) - 1
        require(chapter.getQuests().size() === chapterSizes[chapterIndex], 'Quest count for ' + code)
        chapter.getQuests().forEach(quest => {
          const id = String(quest.getCodeString())
          require(!nodes[id], 'Duplicate quest ' + id)
          nodes[id] = quest
          const parents = []
          quest.streamDependencies().forEach(parent => parents.push(String(parent.getCodeString())))
          dependencies[id] = parents
          if (quest.isOptional()) optional++
          require(quest.getTasksAsList().size() > 0, 'Empty quest ' + id)
          quest.getTasksAsList().forEach(task => {
            if (task instanceof ItemTask) {
              require(!task.getItemStack().isEmpty(), 'Invalid item task ' + task.getCodeString())
            }
          })
          quest.getRewards().forEach(reward => {
            if (reward instanceof ItemReward) {
              require(!reward.getItem().isEmpty(), 'Invalid reward ' + reward.getCodeString())
              books.add(String(reward.getItem().getId()))
            }
            if (reward instanceof CommandReward) gates.push(id)
          })
        })
      })
      require(optional === 114, 'Expected 114 optional quests, got ' + optional)

      const visited = new Set()
      function checkDependencies(id, path) {
        require(nodes[id], 'Unknown dependency ' + id)
        require(!path.has(id), 'Cycle at ' + id)
        if (visited.has(id)) return
        path.add(id)
        dependencies[id].forEach(parent => {
          require(nodes[id].isOptional() || !nodes[parent].isOptional(), 'Optional quest blocks main line: ' + parent)
          checkDependencies(parent, path)
        })
        path.delete(id)
        visited.add(id)
      }

      Object.keys(nodes).forEach(id => checkDependencies(id, new Set()))
      const requiredGates = ['4A10000000000106', '4A50000000000101', '4A60000000000108', '4A70000000000102', '4A80000000000109']
      require(gates.sort().join(',') === requiredGates.join(','), 'Progression reward IDs changed')
      require(dependencies['4A70000000000101'].includes('4A60000000000109'), 'Mature circuit must precede interplanetary logistics')
      require(dependencies['4A80000000000101'].includes('4A7000000000010A'), 'Lunar logistics must precede the Mars base')
      const bootstrap = '4A60000000000129'
      ;['4A60000000000102', '4A60000000000103', '4A60000000000104'].forEach(id => {
        require(dependencies[id].indexOf(bootstrap) >= 0, 'Titanium equipment before bootstrap ' + id)
      })
      ;['immersiveengineering:manual', 'ae2:guide', 'hbm:book_guide_starter', 'deepfield:field_guide'].forEach(id => {
        require(books.has(id), 'Missing guide reward ' + id)
      })
      const SingleRecipeInput = Java.loadClass('net.minecraft.world.item.crafting.SingleRecipeInput')
      const RecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
      const server = context.getSource().getServer()
      const smelting = server.getRecipeManager().getRecipeFor(
        RecipeType.SMELTING, new SingleRecipeInput(Item.of('deepfield:raw_ilmenite')), server.overworld()
      )
      require(smelting.isPresent(), 'Manual titanium bootstrap needs the furnace recipe')
      const output = smelting.get().value().getResultItem(server.registryAccess())
      require(String(output.getId()) === 'deepfield:titanium_ingot', 'Manual smelting must give titanium')
      console.info('[M1-QUEST-POLISH] OK: ' + Object.keys(nodes).length + ' quests, ' + optional + ' optional, four guides and five research gates')
      return 1
    }))
})
