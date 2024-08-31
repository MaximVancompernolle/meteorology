package net.pacmanmvc.meteorology.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(Meteorology.MOD_ID, "ruby"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemgroup.ruby"))
                    .icon(() -> new ItemStack(ModItems.RUBY))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.RUBY);
                        entries.add(ModBlocks.RUBY_BLOCK);
                        entries.add(ModBlocks.RUBY_ORE);
                        entries.add(ModBlocks.DEEPSLATE_RUBY_ORE);
                        entries.add(ModItems.RUBY_SHOVEL);
                        entries.add(ModItems.RUBY_PICKAXE);
                        entries.add(ModItems.RUBY_AXE);
                        entries.add(ModItems.RUBY_HOE);
                        entries.add(ModItems.RUBY_SWORD);
                        entries.add(ModItems.RUBY_HELMET);
                        entries.add(ModItems.RUBY_CHESTPLATE);
                        entries.add(ModItems.RUBY_LEGGINGS);
                        entries.add(ModItems.RUBY_BOOTS);
                    })
                    .build());

    public static final ItemGroup FISHING_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(Meteorology.MOD_ID, "fishing"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemgroup.fishing"))
                    .icon(() -> new ItemStack(ModItems.SUPER_ROD))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.GOOD_ROD);
                        entries.add(ModItems.SUPER_ROD);
                        entries.add(ModItems.FLIPPERS);
                    })
                    .build());

    public static void registerItemGroups() {
        Meteorology.LOGGER.info("Registering Item Groups for " + Meteorology.MOD_ID);
    }
}
