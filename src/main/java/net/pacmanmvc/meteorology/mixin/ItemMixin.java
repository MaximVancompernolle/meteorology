package net.pacmanmvc.meteorology.mixin;

import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "getEnchantability", at = @At("HEAD"), cancellable = true)
    private void getEnchantability(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof ElytraItem) {
            cir.setReturnValue(1);
        }
    }
}
