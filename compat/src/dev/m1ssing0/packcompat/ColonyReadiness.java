package dev.m1ssing0.packcompat;

import com.deepfield.common.machine.MachineKind;
import com.deepfield.neoforge.colony.ColonyCore;
import com.deepfield.neoforge.machine.MachineBlockEntity;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class ColonyReadiness {
    static final ResourceLocation WORLD = ResourceLocation.fromNamespaceAndPath("deepfield", "wolf1069b");
    private static final ResourceLocation CORE = ResourceLocation.fromNamespaceAndPath("m1prototype", "colony/wolf_core");
    private static final ResourceLocation SHIFT = ResourceLocation.fromNamespaceAndPath("m1prototype", "colony/working_shift");

    private ColonyReadiness() {}

    static void inspect(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND
                || !player.isShiftKeyDown() || !event.getItemStack().isEmpty()
                || !WORLD.equals(event.getLevel().dimension().location())) return;
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof MachineBlockEntity machine)
                || machine.kind != MachineKind.COLONY_CORE) return;

        var advancements = player.serverLevel().getServer().getAdvancements();
        award(player, advancements.get(CORE), "placed");
        ColonyCore colony = machine.colony();
        if (!operational(colony)) {
            player.displayClientMessage(Component.translatable("m1packcompat.colony.pending",
                    colony.population(), colony.housing(), colony.lastHaul()), true);
            return;
        }

        award(player, advancements.get(SHIFT), "verified_shift");
        player.displayClientMessage(Component.translatable("m1packcompat.colony.operational"), true);
    }

    private static void award(ServerPlayer player, AdvancementHolder advancement, String criterion) {
        if (advancement != null) player.getAdvancements().award(advancement, criterion);
    }

    static boolean operational(ColonyCore colony) {
        return colony.housing() >= 2 && colony.population() >= 2
                && colony.status() == MachineKind.Status.RUNNING && colony.lastHaul() > 0;
    }
}
