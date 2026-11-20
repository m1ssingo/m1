package dev.m1ssing0.packcompat.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
abstract class RainDelayMixin {
    // The mean clear/rain cycle grows from 114,000 to 152,000 ticks, so rain starts 25% less often.
    @Unique
    private static final IntProvider M1_CLEAR_DELAY = UniformInt.of(50_000, 218_000);

    @Redirect(method = "advanceWeatherCycle", at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.GETSTATIC,
        target = "Lnet/minecraft/server/level/ServerLevel;RAIN_DELAY:Lnet/minecraft/util/valueproviders/IntProvider;"))
    private IntProvider m1$extendClearWeather() {
        return M1_CLEAR_DELAY;
    }
}
