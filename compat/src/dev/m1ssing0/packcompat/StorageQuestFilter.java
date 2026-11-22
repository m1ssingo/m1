package dev.m1ssing0.packcompat;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

final class StorageQuestFilter implements ItemFilterAdapter {
    private static final String MARKER = "m1_storage_by_furnace";
    private static final TagKey<Item> STORAGE = TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath("m1prototype", "storage_by_furnace"));

    static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> FTBQuestsAPI.api().registerFilterAdapter(new StorageQuestFilter()));
    }

    @Override
    public String getName() {
        return "M1 storage by the furnace";
    }

    @Override
    public boolean isFilterStack(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(MARKER);
    }

    @Override
    public boolean doesItemMatch(ItemStack filter, ItemStack candidate, HolderLookup.Provider registries) {
        return candidate.is(STORAGE);
    }

    @Override
    public Matcher getMatcher(ItemStack filter, HolderLookup.Provider registries) {
        return candidate -> candidate.is(STORAGE);
    }

    @Override
    public ItemStack makeTagFilterStack(TagKey<Item> tag) {
        return ItemStack.EMPTY;
    }
}
