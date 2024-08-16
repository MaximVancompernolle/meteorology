package net.pacmanmvc.meteorology.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public int experienceLevel;

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    public void getNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.experienceLevel + 1);
    }
}
