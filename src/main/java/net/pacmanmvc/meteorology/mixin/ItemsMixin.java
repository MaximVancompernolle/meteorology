package net.pacmanmvc.meteorology.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.pacmanmvc.meteorology.item.ModFoodComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;


@Mixin(Items.class)
public class ItemsMixin {
    @ModifyArg(method = "<clinit>",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=glistering_melon_slice")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;<init>(Lnet/minecraft/item/Item$Settings;)V", ordinal = 0))
    private static Item.Settings edibleGlisteringMelon(Item.Settings original) {
        return new Item.Settings().food(ModFoodComponents.GLISTERING_MELON_SLICE);
    }

    @ModifyArg(method = "<clinit>",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=sugar")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;<init>(Lnet/minecraft/item/Item$Settings;)V", ordinal = 0))
    private static Item.Settings edibleSugar(Item.Settings original) {
        return new Item.Settings().food(ModFoodComponents.SUGAR);
    }
}
