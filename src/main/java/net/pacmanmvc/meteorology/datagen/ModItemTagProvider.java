package net.pacmanmvc.meteorology.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.pacmanmvc.meteorology.item.ModItems;
import net.pacmanmvc.meteorology.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.RUBY_HELMET, ModItems.RUBY_CHESTPLATE, ModItems.RUBY_LEGGINGS, ModItems.RUBY_BOOTS)
                .add(ModItems.FLIPPERS);

        getOrCreateTagBuilder(ItemTags.HEAD_ARMOR)
                .add(ModItems.RUBY_HELMET);
        getOrCreateTagBuilder(ItemTags.CHEST_ARMOR)
                .add(ModItems.RUBY_CHESTPLATE);
        getOrCreateTagBuilder(ItemTags.LEG_ARMOR)
                .add(ModItems.RUBY_LEGGINGS);
        getOrCreateTagBuilder(ItemTags.FOOT_ARMOR)
                .add(ModItems.RUBY_BOOTS);
        getOrCreateTagBuilder(ItemTags.FOOT_ARMOR)
                .add(ModItems.FLIPPERS);

        getOrCreateTagBuilder(ItemTags.AXES)
                .add(ModItems.RUBY_AXE);
        getOrCreateTagBuilder(ItemTags.HOES)
                .add(ModItems.RUBY_HOE);
        getOrCreateTagBuilder(ItemTags.PICKAXES)
                .add(ModItems.RUBY_PICKAXE);
        getOrCreateTagBuilder(ItemTags.SHOVELS)
                .add(ModItems.RUBY_SHOVEL);
        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(ModItems.RUBY_SWORD);

        getOrCreateTagBuilder(ItemTags.HOES)
                .add(ModItems.BONE_HOE);

        getOrCreateTagBuilder(ModTags.Items.FISHING_RODS)
                .add(Items.FISHING_ROD, ModItems.GOOD_ROD, ModItems.SUPER_ROD);
    }
}
