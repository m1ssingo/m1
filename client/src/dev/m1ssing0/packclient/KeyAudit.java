package dev.m1ssing0.packclient;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.KeyModifier;

final class KeyAudit {
    private record Binding(String key, KeyModifier modifier) {}
    private static final Map<String, Binding> PLAN = new LinkedHashMap<>();
    static {
        plain("gui.xaero_new_waypoint", "insert");
        plain("chunkloaders.keys.open_screen", "home");
        plain("key.deepfield.star_chart", "end");
        plain("key.ftbquests.quests", "grave.accent");
        alt("key.ae2.wireless_terminal", "e");
        alt("key.mekanism.head_mode", "v");
        alt("key.mekanism.chest_mode", "g");
        alt("key.mekanism.legs_mode", "j");
        alt("key.mekanism.feet_mode", "b");
        alt("key.mekanism.key_hud", "h");
        alt("key.mekanism.mode", "n");
        alt("key.hbm.calculator", "c");
        alt("key.hbm.toggleHUD", "u");
        alt("key.hbm.toggleMagnet", "m");
        alt("key.deepfield.tech.mode", "r");
        alt("key.deepfield.tech.hover", "t");
        alt("key.jetpack_activate", "k");
        alt("key.jetpack_hover", "i");
        alt("key.jetpack_hud", "l");
    }
    private KeyAudit() {}
    private static void plain(String name, String key) {
        PLAN.put(name, new Binding("key.keyboard." + key, KeyModifier.NONE));
    }
    private static void alt(String name, String key) {
        PLAN.put(name, new Binding("key.keyboard." + key, KeyModifier.ALT));
    }
    private static IKeyMappingExtension extension(KeyMapping key) {
        return (IKeyMappingExtension) (Object) key;
    }
    static void apply(Minecraft minecraft) {
        List<KeyMapping> keys = Arrays.asList(minecraft.options.keyMappings);
        Path directory = minecraft.gameDirectory.toPath().resolve("config");
        Path marker = directory.resolve("m1pack-keybindings-030.applied");
        List<Map<String, String>> changes = new ArrayList<>();
        if (!Files.exists(marker)) {
            for (KeyMapping mapping : keys) {
                Binding target = PLAN.get(mapping.getName());
                if (target == null || !mapping.isDefault()) continue;
                String before = extension(mapping).getKeyModifier() + ":" + mapping.saveString();
                extension(mapping).setKeyModifierAndCode(target.modifier(), InputConstants.getKey(target.key()));
                changes.add(Map.of("action", mapping.getName(), "from", before,
                    "to", extension(mapping).getKeyModifier() + ":" + mapping.saveString()));
            }
            KeyMapping.resetMapping();
            if (!changes.isEmpty()) minecraft.options.save();
        }
        List<Map<String, String>> overlaps = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                KeyMapping a = keys.get(i), b = keys.get(j);
                if (!a.isUnbound() && !b.isUnbound() && a.same(b)
                    && extension(a).getKeyModifier() == extension(b).getKeyModifier()) {
                    overlaps.add(Map.of("first", a.getName(), "second", b.getName(), "key", a.saveString()));
                }
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("version", "0.3.0");
        report.put("changes_this_launch", changes);
        report.put("shared_keys_including_contextual_actions", overlaps);
        report.put("bindings", keys.stream().map(key -> Map.of("name", key.getName(),
            "key", key.saveString(), "modifier", extension(key).getKeyModifier().toString(),
            "default", key.getDefaultKey().getName(), "category", key.getCategory())).toList());
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("m1pack-keybindings.json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(report));
            Files.writeString(marker, "0.3.0\n");
        } catch (Exception error) {
            LogUtils.getLogger().warn("M1 key audit report could not be saved", error);
        }
        LogUtils.getLogger().info("[M1-KEYS] {} default bindings updated; {} shared keys including contextual actions",
            changes.size(), overlaps.size());
    }
}
