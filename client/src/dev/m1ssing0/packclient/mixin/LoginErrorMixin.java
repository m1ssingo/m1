package dev.m1ssing0.packclient.mixin;

import com.mojang.logging.LogUtils;
import java.util.Locale;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientHandshakePacketListenerImpl.class)
abstract class LoginErrorMixin {
    @Inject(method = "authenticateServer", at = @At("RETURN"), cancellable = true)
    private void m1packclient$explainHtmlResponse(String serverHash, CallbackInfoReturnable<Component> callback) {
        Component reason = callback.getReturnValue();
        if (reason == null || !(reason.getContents() instanceof TranslatableContents message)
            || !message.getKey().equals("disconnect.loginFailedInfo")
            || message.getArgs().length != 1 || !(message.getArgs()[0] instanceof String error)) {
            return;
        }

        // Authlib 6.0.54 embeds failed JSON input in the exception message.
        String prefix = "com.mojang.authlib.exceptions.MinecraftClientException: Failed to read value ";
        if (!error.startsWith(prefix)) {
            return;
        }
        String html = error.substring(prefix.length()).stripLeading().toLowerCase(Locale.ROOT);
        if (!html.startsWith("<!doctype html") && !html.startsWith("<html")) {
            return;
        }

        LogUtils.getLogger().warn("Minecraft session join received HTML instead of an auth response");
        callback.setReturnValue(Component.translatable("disconnect.loginFailedInfo",
            Component.translatable("disconnect.loginFailedInfo.serversUnavailable")));
    }
}
