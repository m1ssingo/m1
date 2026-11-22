package dev.m1ssing0.packclient.mixin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLanguage.class)
abstract class ClientLanguageMixin {
    @Unique private static final ResourceLocation M1_PETROLEUM_RUSSIAN =
        ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "lang/ru_ru.json");
    @Unique private static final Pattern M1_MISSING_COMMA = Pattern.compile(
        "(?m)^(\\h*\"(?:[^\"\\\\]|\\\\.)*\"\\h*:\\h*\"(?:[^\"\\\\]|\\\\.)*\")"
            + "(\\s*)(?=\"(?:[^\"\\\\]|\\\\.)*\"\\h*:)");
    @Unique private static final Pattern M1_TRAILING_COMMA = Pattern.compile(",(?=\\s*\\}\\s*\\z)");

    @Redirect(method = "loadFrom", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResourceStack"
            + "(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/List;"))
    private static List<Resource> m1$fixPetroleumTranslation(ResourceManager manager, ResourceLocation id) {
        var resources = manager.getResourceStack(id);
        if (!M1_PETROLEUM_RUSSIAN.equals(id)) return resources;
        return resources.stream().map(resource -> new Resource(resource.source(),
            () -> m1$openRepairedTranslation(resource), resource::metadata)).toList();
    }

    @Unique
    private static InputStream m1$openRepairedTranslation(Resource resource) throws IOException {
        try (var stream = resource.open()) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            // The Russian language file in 4.5.0-39 has two missing separators and a trailing comma.
            String repaired = M1_MISSING_COMMA.matcher(json).replaceAll("$1,$2");
            repaired = M1_TRAILING_COMMA.matcher(repaired).replaceFirst("");
            return new ByteArrayInputStream(repaired.getBytes(StandardCharsets.UTF_8));
        }
    }
}
