package net.pacmanmvc.meteorology.loot;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;

public class ModLootTables {

    public static final RegistryKey<LootTable> GOOD_FISHING_LOOT_TABLE = registerLootTable("gameplay/good_fishing");

    private static RegistryKey<LootTable> registerLootTable(String name) {
        return LootTables.registerLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(Meteorology.MOD_ID, name)));
    }

    public static void registerModLootTables() {
        Meteorology.LOGGER.info("Registering Mod Loot Tables for " + Meteorology.MOD_ID);
    }

}
