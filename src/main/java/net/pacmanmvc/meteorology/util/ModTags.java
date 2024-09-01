package net.pacmanmvc.meteorology.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;

public class ModTags {
    public static class Blocks {
        public static TagKey<Block> RUBY_ORES = createTag("ruby_ores");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Meteorology.MOD_ID, name));
        }
    }

    public static class Items {
        public static TagKey<Item> CUSTOM_RODS = createTag("custom_rods");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(Meteorology.MOD_ID, name));
        }
    }

    public static void registerModTags() {
        Meteorology.LOGGER.info("Registering Mod Tags for " + Meteorology.MOD_ID);
    }
}
