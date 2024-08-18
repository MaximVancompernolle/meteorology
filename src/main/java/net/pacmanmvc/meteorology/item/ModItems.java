package net.pacmanmvc.meteorology.item;

import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;

public class ModItems {
    public static final Item RUBY = registerItem("ruby", new Item(new Item.Settings()));
    public static final Item RAW_RUBY = registerItem("raw_ruby", new Item(new Item.Settings()));

    public static final Item RUBY_AXE = registerItem("ruby_axe", new AxeItem(ModToolMaterial.RUBY, new Item.Settings().
            attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterial.RUBY, 6.0F, -3.0F))));
    public static final Item RUBY_HOE = registerItem("ruby_hoe", new HoeItem(ModToolMaterial.RUBY, new Item.Settings()
            .attributeModifiers(HoeItem.createAttributeModifiers(ModToolMaterial.RUBY, -5.0F, 0.0F))));
    public static final Item RUBY_PICKAXE = registerItem("ruby_pickaxe", new PickaxeItem(ModToolMaterial.RUBY, new Item.Settings()
            .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterial.RUBY, -1.0F, -2.8F))));
    public static final Item RUBY_SHOVEL = registerItem("ruby_shovel", new ShovelItem(ModToolMaterial.RUBY, new Item.Settings()
            .attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterial.RUBY, -0.5F, -3.0F))));
    public static final Item RUBY_SWORD = registerItem("ruby_sword", new SwordItem(ModToolMaterial.RUBY, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterial.RUBY, 4, -2.4F))));

    public static final Item RUBY_HELMET = registerItem("ruby_helmet", new ArmorItem(ModArmorMaterial.RUBY, ArmorItem.Type.HELMET, new Item.Settings()
            .maxDamage(ArmorItem.Type.HELMET.getMaxDamage(10))));
    public static final Item RUBY_CHESTPLATE = registerItem("ruby_chestplate", new ArmorItem(ModArmorMaterial.RUBY, ArmorItem.Type.CHESTPLATE, new Item.Settings()
            .maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(10))));
    public static final Item RUBY_LEGGINGS = registerItem("ruby_leggings", new ArmorItem(ModArmorMaterial.RUBY, ArmorItem.Type.LEGGINGS, new Item.Settings()
            .maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(10))));
    public static final Item RUBY_BOOTS = registerItem("ruby_boots", new ArmorItem(ModArmorMaterial.RUBY, ArmorItem.Type.BOOTS, new Item.Settings()
            .maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(10))));

    public static final Item GOOD_ROD = registerItem("good_rod", new FishingRodItem(new Item.Settings()));
    public static final Item SUPER_ROD = registerItem("super_rod", new FishingRodItem(new Item.Settings()));
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Meteorology.MOD_ID, name), item);
    }
    public static void registerModItems() {
        Meteorology.LOGGER.info("Registering Mod Items for " + Meteorology.MOD_ID);
    }
}
