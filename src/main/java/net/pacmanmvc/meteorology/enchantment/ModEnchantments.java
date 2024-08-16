package net.pacmanmvc.meteorology.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.enchantment.effect.ZoomEnchantmentEffect;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> ZOOM_KEY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(Meteorology.MOD_ID, "zoom"));
    public static final MapCodec<ZoomEnchantmentEffect> ZOOM_EFFECT = register("zoom", ZoomEnchantmentEffect.CODEC);

    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(Meteorology.MOD_ID, name), codec);
    }

    public static void registerModEnchantments() {
        Meteorology.LOGGER.info("Registering Mod Enchantments for " + Meteorology.MOD_ID);
    }
}
