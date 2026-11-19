package dev.m1ssing0.packcompat;

import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.event.MekanismTeleportEvent;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod("m1packcompat")
public final class InterplanetaryTravel {
    private static final String STAGE = "m1_interplanetary_logistics";
    private static final ResourceLocation MOON = ResourceLocation.fromNamespaceAndPath("deepfield", "moon");
    private static final ResourceLocation OUTBOUND = id("earth_to_moon");
    private static final ResourceLocation RETURN = id("moon_to_earth");
    private final Map<ServerPlayer, Crossing> departures = new WeakHashMap<>();

    public InterplanetaryTravel() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::beforeTravel);
        NeoForge.EVENT_BUS.addListener(this::afterTravel);
    }

    private void beforeTravel(MekanismTeleportEvent.GlobalTeleport event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.isTransDimensional()) return;
        var server = player.serverLevel().getServer();
        if (!StageStates.isUnlockedGlobal(STAGE, server.overworld())) {
            event.setCanceled(true);
            return;
        }
        if (!(event instanceof MekanismTeleportEvent.Teleporter)
                || event instanceof MekanismTeleportEvent.PortableTeleporter) return;
        var from = player.level().dimension();
        var to = event.getTargetDimension();
        var advancement = route(from, to);
        if (advancement != null) {
            departures.put(player, new Crossing(from, to, event.getTarget(), server.getTickCount(), advancement));
        }
    }

    private void afterTravel(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var crossing = departures.remove(player);
        var server = player.serverLevel().getServer();
        if (crossing == null || !crossing.from().equals(event.getFrom()) || !crossing.to().equals(event.getTo())
                || crossing.tick() != server.getTickCount()
                || player.position().distanceToSqr(crossing.target()) > 4.0) return;
        var advancement = server.getAdvancements().get(crossing.advancement());
        if (advancement != null) player.getAdvancements().award(advancement, "linked_transit");
    }

    private static ResourceLocation route(ResourceKey<Level> from, ResourceKey<Level> to) {
        if (Level.OVERWORLD.equals(from) && MOON.equals(to.location())) return OUTBOUND;
        if (MOON.equals(from.location()) && Level.OVERWORLD.equals(to)) return RETURN;
        return null;
    }

    private static ResourceLocation id(String route) {
        return ResourceLocation.fromNamespaceAndPath("m1prototype", "logistics/" + route);
    }

    private record Crossing(ResourceKey<Level> from, ResourceKey<Level> to, Vec3 target,
                            int tick, ResourceLocation advancement) {}
}
