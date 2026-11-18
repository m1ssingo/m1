// Diagnostic operator command; no automatic changes to the world.
ServerEvents.commandRegistry(event => {
  event.register(event.commands.literal('m1probe')
    .requires(source => source.hasPermission(2))
    .executes(context => {
      const server = context.getSource().getServer()
      const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
      const StageStates = Java.loadClass('net.bananemdnsa.historystages.api.stage.StageStates')
      const manager = server.getRecipeManager()
      const visible = id => {
        const iterator = manager.getRecipes().iterator()
        while (iterator.hasNext()) {
          if (String(iterator.next().id()) === id) return true
        }
        return false
      }
      const result = {
        stage_unlocked: StageStates.isUnlockedGlobal('m1_proto_electronics', server.overworld()),
        basic_circuit_recipe: visible('mekanism:control_circuit/basic'),
        controller_recipe: visible('ae2:network/blocks/controller'),
        basic_recipe_retained: manager.byKey(ResourceLocation.parse('mekanism:control_circuit/basic')).isPresent(),
        controller_recipe_retained: manager.byKey(ResourceLocation.parse('ae2:network/blocks/controller')).isPresent(),
        total_recipes: manager.getRecipes().size()
      }
      result.world_checks = String(server.getCommandStorage().get(ResourceLocation.parse('m1prototype:checks')))
      console.info('[M1-PROBE] ' + JSON.stringify(result))
      return 1
    }))
})
