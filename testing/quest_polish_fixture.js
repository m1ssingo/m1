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
    const chapterSizes = [18, 33, 29, 24, 23, 26, 26, 28, 32]
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
    require(chapters.size() === chapterSizes.length, 'All nine chapters must load')
    chapters.forEach(chapter => {
      const code = String(chapter.getCodeString())
      const chapterIndex = Number(code.charAt(2))
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
        const description = translations.getStringListTranslation(quest, 'ru_ru', key.QUEST_DESC).orElseThrow()
        require(description.size() > 0 && description.size() <= 4, 'Invalid description ' + id)
        description.forEach(line => {
          if (String(line).startsWith('{')) {
            const component = text.parseRawText(String(line), registry)
            const click = component.getStyle().getClickEvent()
            require(click !== null && String(click.getAction().getSerializedName()) === 'change_page', 'Unreadable guide link ' + id)
            links.push(String(click.getValue()))
          }
        })
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
    require(optional === 155, 'Expected 155 optional quests, got ' + optional)
    require(groups.size === 3, 'Missing chapter groups')
    require(links.length === 18 && links.every(id => nodes[id]), 'Broken in-book guide link')

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
    const advanceId = Java.loadClass('net.minecraft.resources.ResourceLocation')
    ;['minecraft:adventure/sleep_in_bed', 'mutantmonsters:burn_zombie_burn'].forEach(id => {
      require(server.getAdvancements().get(advanceId.parse(id)) !== null, 'Missing advancement ' + id)
    })
    console.info('[M1-QUEST-POLISH] OK: ' + Object.keys(nodes).length + ' quests, ' + optional + ' optional, ' + links.length + ' in-book links, three groups and five research gates')
    return 1
  }
})
