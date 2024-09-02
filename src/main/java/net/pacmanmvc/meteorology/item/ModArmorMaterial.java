package net.pacmanmvc.meteorology.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModArmorMaterial {
    public static final RegistryEntry<ArmorMaterial> RUBY = register("ruby", Map.of(
            ArmorItem.Type.HELMET, 4,
            ArmorItem.Type.CHESTPLATE, 10,
            ArmorItem.Type.LEGGINGS, 7,
            ArmorItem.Type.BOOTS, 4
    ), 12, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 4.0F, 0.2F, () -> Ingredient.ofItems(ModItems.RUBY));
    public static RegistryEntry<ArmorMaterial> register(String id,
                                                        Map<ArmorItem.Type, Integer> defense,
                                                        int enchantability,
                                                        RegistryEntry<SoundEvent> equipSound,
                                                        float toughness,
                                                        float knockbackResistance,
                                                        Supplier<Ingredient> repairIngredient) {
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Identifier.of(Meteorology.MOD_ID, id)));

        return RegistryEntry.of(Registry.register(Registries.ARMOR_MATERIAL, Identifier.of(Meteorology.MOD_ID, id),
                new ArmorMaterial(defense, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance)));
    }
}
