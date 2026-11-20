package dev.m1ssing0.packclient;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

@JeiPlugin
public final class QuestRecipeBridge implements IModPlugin {
    private final Helper helper = new Helper();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("m1packclient", "quest_recipes");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        helper.runtime = runtime;
        if (!ModList.get().isLoaded("ftbxmodcompat")
                && FTBQuests.getRecipeModHelper().getClass() == RecipeModHelper.NoOp.class) {
            FTBQuests.setRecipeModHelper(helper);
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        helper.runtime = null;
    }

    private static final class Helper extends RecipeModHelper.NoOp {
        private IJeiRuntime runtime;

        @Override
        public boolean isRecipeModAvailable() {
            return runtime != null;
        }

        @Override
        public String getHelperName() {
            return "JEI";
        }

        @Override
        public void showRecipes(ItemStack stack) {
            var activeRuntime = runtime;
            if (activeRuntime == null || stack.isEmpty()) return;

            var focus = activeRuntime.getJeiHelpers().getFocusFactory().createFocus(
                RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack.copyWithCount(1));
            activeRuntime.getRecipesGui().show(focus);
        }
    }
}
