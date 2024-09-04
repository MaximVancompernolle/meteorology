package net.pacmanmvc.meteorology;

import net.fabricmc.api.ModInitializer;
import net.pacmanmvc.meteorology.block.ModBlocks;
import net.pacmanmvc.meteorology.enchantment.ModEnchantments;
import net.pacmanmvc.meteorology.entity.ModEntities;
import net.pacmanmvc.meteorology.item.ModItemGroups;
import net.pacmanmvc.meteorology.item.ModItems;
import net.pacmanmvc.meteorology.loot.ModLootTables;
import net.pacmanmvc.meteorology.util.ModTags;
import net.pacmanmvc.meteorology.village.ModTradeOffers;
import net.pacmanmvc.meteorology.world.gen.ModWorldGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Meteorology implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "meteorology";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final int SPONGE_HOLD_TIME_SECONDS = 5;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModEntities.registerModEntities();
		ModItemGroups.registerItemGroups();
		ModTags.registerModTags();
		ModEnchantments.registerModEnchantments();
		ModLootTables.registerModLootTables();
		ModTradeOffers.registerTradeOffers();
		ModWorldGeneration.generateModWorldGen();
	}
}