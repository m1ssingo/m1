package dev.m1ssing0.packcompat.mixin;

import java.util.function.BooleanSupplier;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {TileEntityMetallurgicInfuser.class, TileEntityItemStackChemicalToItemStackFactory.class},
       remap = false)
public abstract class MekanismElectronicsMixin {
    @Redirect(
        method = "createNewCachedRecipe(Lmekanism/api/recipes/ItemStackChemicalToItemStackRecipe;I)Lmekanism/api/recipes/cache/CachedRecipe;",
        at = @At(value = "INVOKE", target = "Lmekanism/api/recipes/cache/CachedRecipe;setCanHolderFunction(Ljava/util/function/BooleanSupplier;)Lmekanism/api/recipes/cache/CachedRecipe;")
    )
    private CachedRecipe<?> m1compat$requireElectronics(
        CachedRecipe<?> cached, BooleanSupplier canRun, ItemStackChemicalToItemStackRecipe recipe, int slot
    ) {
        var basicCircuit = MekanismItems.BASIC_CONTROL_CIRCUIT.get();
        if (recipe.getOutputDefinition().stream().noneMatch(output -> output.is(basicCircuit))) {
            return cached.setCanHolderFunction(canRun);
        }

        // A reload can leave an active operation on its old cached recipe.
        // The machine must check the live stage before consuming or producing.
        BlockEntity machine = (BlockEntity) (Object) this;
        return cached.setCanHolderFunction(() ->
            canRun.getAsBoolean()
                && machine.getLevel() instanceof ServerLevel level
                && StageStates.isUnlockedGlobal("m1_proto_electronics", level.getServer().overworld())
        );
    }
}
