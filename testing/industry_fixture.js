// Test server only. The release does not install this operator command.
ServerEvents.commandRegistry(event => {
  event.register(event.commands.literal('m1industryfixture')
    .requires(source => source.hasPermission(4))
    .executes(context => {
      try {
      var fixtureServer = context.getSource().getServer()
      var QuestFile = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile')
      var FakePlayers = Java.loadClass('net.neoforged.neoforge.common.util.FakePlayerFactory')
      var ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
      var StageStates = Java.loadClass('net.bananemdnsa.historystages.api.stage.StageStates')
      var manager = fixtureServer.getRecipeManager()
      var overworld = fixtureServer.overworld()
      var matches = (id, item) => manager.byKey(ResourceLocation.parse(id)).orElseThrow()
        .value().getIngredients().stream().filter(ingredient => ingredient.test(Item.of(item))).count()

      var chapter = null
      QuestFile.getInstance().orElseThrow().getAllChapters().forEach(value => {
        if (String(value.getCodeString()) === '4A10000000000001') chapter = value
      })
      if (chapter === null) throw new Error('Missing first industry chapter')
      var chemistry = null
      QuestFile.getInstance().orElseThrow().getAllChapters().forEach(value => {
        if (String(value.getCodeString()) === '4A20000000000001') chemistry = value
      })
      if (chemistry === null) throw new Error('Missing chemistry chapter')
      var ItemTask = Java.loadClass('dev.ftb.mods.ftbquests.quest.task.ItemTask')
      chemistry.getQuests().forEach(quest => quest.getTasksAsList().forEach(task => {
        if (task instanceof ItemTask && task.getItemStack().isEmpty()) {
          throw new Error('Invalid item task in chemistry chapter')
        }
      }))
      var quests = chapter.getQuests()
      var unlock = quests.get(5)
      var reward = unlock.getRewards().iterator().next()
      var before = StageStates.isUnlockedGlobal('m1_proto_electronics', overworld)
      var claimed = reward.claim(FakePlayers.getMinecraft(overworld), false)
      console.info('[M1-INDUSTRY] ' + JSON.stringify({
        quests: quests.size(),
        chemistry_quests: chemistry.getQuests().size(),
        chapter_title: String(chapter.getRawTitle()),
        chapter_final_title: String(quests.get(8).getRawTitle()),
        unlock_id: String(unlock.getCodeString()),
        team_reward: reward.isTeamReward(),
        reward_claimed: claimed,
        stage_before: before,
        separator_advanced_slots: matches('mekanism:electrolytic_separator', 'mekanism:advanced_control_circuit'),
        infuser_steel_slots: matches('mekanism:metallurgic_infuser', 'immersiveengineering:component_steel'),
        acceptor_basic_slots: matches('ae2:network/blocks/energy_energy_acceptor', 'mekanism:basic_control_circuit'),
        controller_basic_slots: matches('ae2:network/blocks/controller', 'mekanism:basic_control_circuit')
      }))
      return 1
      } catch (error) {
        console.error(String(error) + " " + error.stack)
        throw error
      }
    }))
})
