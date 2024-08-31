package net.pacmanmvc.meteorology.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.gen.GenerationStep;
import net.pacmanmvc.meteorology.world.ModPlacedFeatures;

public class ModOreGeneration {
    public static void generateOres() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN), GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_RUBY);
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN), GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_RUBY_LARGE);
    }
}
