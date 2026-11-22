package dev.m1ssing0.packcompat;

import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.event.MekanismTeleportEvent;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("m1packcompat")
public final class InterplanetaryTravel {
    private static final String STAGE = "m1_interplanetary_logistics";
    private static final String MARS_STAGE = "m1_mars_logistics";
    private static final String COLONY_STAGE = "m1_colony_logistics";
    private static final ResourceLocation MOON = ResourceLocation.fromNamespaceAndPath("deepfield", "moon");
    private static final ResourceLocation MARS = ResourceLocation.fromNamespaceAndPath("deepfield", "mars");
    private static final ResourceLocation EARTH_TO_MOON = id("earth_to_moon");
    private static final ResourceLocation MOON_TO_EARTH = id("moon_to_earth");
    private static final ResourceLocation EARTH_TO_MARS = id("earth_to_mars");
    private static final ResourceLocation MARS_TO_EARTH = id("mars_to_earth");
    private static final ResourceLocation EARTH_TO_COLONY = id("earth_to_colony");
    private static final ResourceLocation COLONY_TO_EARTH = id("colony_to_earth");
    private final Map<ServerPlayer, Crossing> departures = new WeakHashMap<>();

    public InterplanetaryTravel(IEventBus modBus) {
        modBus.addListener(StorageQuestFilter::setup);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::beforeTravel);
        NeoForge.EVENT_BUS.addListener(this::afterTravel);
        NeoForge.EVENT_BUS.addListener(this::setDayLength);
        // Inspect the core before Deepfield handles the same click to open colony management.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ColonyReadiness::inspect);
    }

    private void setDayLength(ServerStartedEvent event) {
        event.getServer().overworld().setDayTimePerTick(2.0f / 3.0f);
    }

    private void beforeTravel(MekanismTeleportEvent.GlobalTeleport event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.isTransDimensional()) return;
        var server = player.serverLevel().getServer();
        var from = player.level().dimension();
        var to = event.getTargetDimension();
        departures.remove(player);
        if (!StageStates.isUnlockedGlobal(STAGE, server.overworld())
                || (involves(from, to, MARS) && !StageStates.isUnlockedGlobal(MARS_STAGE, server.overworld()))) {
            event.setCanceled(true);
            return;
        }
        if (involves(from, to, ColonyReadiness.WORLD) && !StageStates.isUnlockedGlobal(COLONY_STAGE, server.overworld())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("m1packcompat.colony.link_locked"), true);
            return;
        }
        if (!(event instanceof MekanismTeleportEvent.Teleporter)
                || event instanceof MekanismTeleportEvent.PortableTeleporter) return;
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
        if (Level.OVERWORLD.equals(from)) {
            if (MOON.equals(to.location())) return EARTH_TO_MOON;
            if (MARS.equals(to.location())) return EARTH_TO_MARS;
            if (ColonyReadiness.WORLD.equals(to.location())) return EARTH_TO_COLONY;
        } else if (Level.OVERWORLD.equals(to)) {
            if (MOON.equals(from.location())) return MOON_TO_EARTH;
            if (MARS.equals(from.location())) return MARS_TO_EARTH;
            if (ColonyReadiness.WORLD.equals(from.location())) return COLONY_TO_EARTH;
        }
        return null;
    }

    private static boolean involves(ResourceKey<Level> from, ResourceKey<Level> to, ResourceLocation dimension) {
        return dimension.equals(from.location()) || dimension.equals(to.location());
    }

    private static ResourceLocation id(String route) {
        return ResourceLocation.fromNamespaceAndPath("m1prototype", "logistics/" + route);
    }

    private record Crossing(ResourceKey<Level> from, ResourceKey<Level> to, Vec3 target,
                            int tick, ResourceLocation advancement) {}
}
