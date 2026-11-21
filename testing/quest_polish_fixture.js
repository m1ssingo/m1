// Copy this fixture to a disposable server's kubejs/server_scripts before running it.
ServerEvents.commandRegistry(event => {
  event.register(event.commands.literal('m1questpolishfixture')
    .requires(source => source.hasPermission(4))
    .executes(context => {
      try {
        return verify(context)
      } catch (error) {
        console.error('[M1-QUEST-POLISH] ' + String(error))
        throw error
      }
    }))

  ;[false, true].forEach(open => {
    const commandName = open ? 'm1radioopenfixture' : 'm1radiolockedfixture'
    event.register(event.commands.literal(commandName).requires(source => source.hasPermission(4))
      .executes(context => checkRadio(context.getSource().getServer(), open)))
  })

  function checkRadio(server, open) {
    const CraftingInput = Java.loadClass('net.minecraft.world.item.crafting.CraftingInput')
    const RecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const ArrayList = Java.loadClass('java.util.ArrayList')
    const symbols = {I:'minecraft:iron_ingot', C:'minecraft:copper_ingot', R:'minecraft:redstone',
      B:'mekanism:basic_control_circuit', N:'minecraft:note_block', P:'minecraft:oak_planks', S:'minecraft:paper', '.':'minecraft:air'}
    const patterns = {radio_block:'ICIRBRINI', speaker_block:'.P.PNP.C.', tuner:'..C.R.I..', memory_card:'R..S.....'}
    const valid = Object.keys(patterns).every(name => {
      const entries = new ArrayList()
      patterns[name].split('').forEach(symbol => entries.add(Item.of(symbols[symbol])))
      const recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
        CraftingInput.of(3, 3, entries), server.overworld())
      const matches = recipe.isPresent() && String(recipe.get().id()) === 'm1prototype:openfm/' + name
      if (matches !== open || (matches && String(recipe.get().value().getResultItem(server.registryAccess()).getId()) !== 'openfm:' + name)) {
        console.warn('[M1-RADIO-QA] Unexpected match for ' + name + ' with research ' + open + ', got ' + (recipe.isPresent() ? String(recipe.get().id()) : 'no recipe'))
        return false
      }
      return true
    })
    if (!valid) return 0
    console.info('[M1-RADIO-QA] All four crafting grids ' + (open ? 'open' : 'locked'))
    return 1
  }

  function verify(context) {
    const QuestFile = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile')
    const ItemTask = Java.loadClass('dev.ftb.mods.ftbquests.quest.task.ItemTask')
    const ItemReward = Java.loadClass('dev.ftb.mods.ftbquests.quest.reward.ItemReward')
    const CommandReward = Java.loadClass('dev.ftb.mods.ftbquests.quest.reward.CommandReward')
    const dependencies = {}
    const nodes = {}
    const chapterSizes = [18, 40, 32, 28, 26, 28, 29, 31, 35, 12, 8]
    chapterSizes[0xD] = 24
    const groups = new Set()
    const links = []
    const books = new Set()
    const gates = []
    let optional = 0

    function require(condition, reason) {
      if (!condition) throw new Error(reason)
    }

    const file = QuestFile.getInstance().orElseThrow()
    const translations = file.getTranslationManager()
    const key = Java.loadClass('dev.ftb.mods.ftbquests.quest.translation.TranslationKey')
    const text = Java.loadClass('dev.ftb.mods.ftbquests.util.TextUtils')
    const registry = context.getSource().getServer().registryAccess()
    const chapters = file.getAllChapters()
    require(chapters.size() === Object.keys(chapterSizes).length, 'All twelve chapters must load')
    chapters.forEach(chapter => {
      const code = String(chapter.getCodeString())
      const chapterIndex = parseInt(code.charAt(2), 16)
      const group = chapter.getGroup()
      const groupTitle = translations.getStringTranslation(group, 'ru_ru', key.TITLE).orElseThrow()
      groups.add(String(groupTitle))
      require(chapter.getQuests().size() === chapterSizes[chapterIndex], 'Quest count for ' + code)
      chapter.getQuests().forEach(quest => {
        const id = String(quest.getCodeString())
        require(!nodes[id], 'Duplicate quest ' + id)
        nodes[id] = quest
        const title = String(translations.getStringTranslation(quest, 'ru_ru', key.TITLE).orElseThrow())
        require(title.length > 2 && title === String(translations.getStringTranslation(quest, 'en_us', key.TITLE).orElseThrow()), 'Missing title or stale fallback ' + id)
        const description = translations.getStringListTranslation(quest, 'ru_ru', key.QUEST_DESC)
        require(description.isPresent(), 'Missing description ' + id)
        const fallback = translations.getStringListTranslation(quest, 'en_us', key.QUEST_DESC).orElseThrow()
        require(description.get().equals(fallback), 'Stale description fallback ' + id)
        let hasProse = false
        description.get().forEach(paragraph => {
          const line = String(paragraph)
          if (line.startsWith('{')) {
            const component = text.parseRawText(line, registry)
            const click = component.getStyle().getClickEvent()
            require(click !== null && String(click.getAction().getSerializedName()) === 'change_page', 'Unreadable guide link ' + id)
            links.push(String(click.getValue()))
          } else if (line.trim()) hasProse = true
        })
        require(hasProse, 'Missing quest text ' + id)
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
    require(optional === 207, 'Expected 207 optional quests, got ' + optional)
    require(groups.size === 4 && groups.has('Разное'), 'Missing chapter groups')
    require(links.length === 19 && links.every(id => nodes[id]), 'Broken in-book guide link')

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
    const mainPath = new Set()
    function traceFinal(id) {
      if (mainPath.has(id)) return
      mainPath.add(id)
      dependencies[id].forEach(traceFinal)
    }
    traceFinal('4A8000000000010C')
    require(mainPath.size === 84, 'A required quest is disconnected from the Mars finale')
    const firstPower = '4A10000000000104'
    ;['4A1000000000010B', '4A10000000000138', '4A10000000000105'].forEach(id => {
      require(dependencies[id].length === 1 && dependencies[id][0] === firstPower, 'Workshop branch has an extra gate ' + id)
    })
    const poweredPress = '4A1000000000013E'
    require(!mainPath.has(poweredPress) && dependencies[poweredPress].length === 2
      && dependencies[poweredPress].includes('4A10000000000139')
      && dependencies[poweredPress].includes('4A1000000000010C'), 'The press-project task must join the mold and the LV buffer')
    function expectParents(chapter, suffix, parents) {
      const id = part => part.length === 3 ? '4A' + chapter + '0000000000' + part : part
      require(dependencies[id(suffix)].slice().sort().join(',') === parents.map(id).sort().join(','),
        'Incorrect branch convergence at ' + id(suffix))
    }
    expectParents('2', '141', ['10B', '10C'])
    expectParents('2', '142', ['102', '10A'])
    expectParents('3', '142', ['143'])
    expectParents('3', '126', ['142'])
    expectParents('4', '106', ['102', '103', '105'])
    expectParents('4', '124', ['10B', '123'])
    expectParents('5', '107', ['103', '105', '106'])
    expectParents('6', '140', ['106', '120'])
    expectParents('7', '104', ['103'])
    expectParents('7', '108', ['105', '106', '107'])
    expectParents('8', '140', ['132', '142'])
    expectParents('9', '141', ['101', '4A00000000000118'])
    expectParents('D', '101', ['4A8000000000010C'])
    expectParents('D', '10A', ['109', '118'])
    expectParents('D', '110', ['107', '10B', '10F'])
    traceFinal('4AD0000000000112')
    require(mainPath.size === Object.keys(nodes).length - optional, 'A required quest is disconnected from the offworld workshop')
    const requiredGates = ['4A10000000000106', '4A50000000000101', '4A60000000000108', '4A70000000000102', '4A80000000000109', '4AD0000000000112']
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
    const advanceId = Java.loadClass('net.minecraft.resources.ResourceLocation')
    ;['minecraft:adventure/sleep_in_bed', 'mutantmonsters:burn_zombie_burn', 'm1prototype:industry/mars_reactor_controller'].forEach(id => {
      require(server.getAdvancements().get(advanceId.parse(id)) !== null, 'Missing advancement ' + id)
    })
    console.info('[M1-QUEST-POLISH] OK: ' + Object.keys(nodes).length + ' quests, ' + optional + ' optional, ' + links.length + ' in-book links, four groups and six research gates')
    return 1
  }
})
