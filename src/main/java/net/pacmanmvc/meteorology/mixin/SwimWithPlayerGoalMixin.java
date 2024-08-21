package net.pacmanmvc.meteorology.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pacmanmvc.meteorology.item.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.passive.DolphinEntity$SwimWithPlayerGoal")
public abstract class SwimWithPlayerGoalMixin extends Goal {
    @Shadow
    @Final
    private DolphinEntity dolphin;
    @Shadow
    private PlayerEntity closestPlayer;

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    public void shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.closestPlayer != null && this.closestPlayer.isSwimming() && this.dolphin.squaredDistanceTo(this.closestPlayer) < 512);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.closestPlayer.isSwimming()
                && this.closestPlayer.getWorld().random.nextInt(6) == 0
                && this.closestPlayer.getStatusEffect(StatusEffects.DOLPHINS_GRACE) != null
                && this.closestPlayer.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.FLIPPERS)) {
            this.closestPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 100, 1), this.dolphin);
        }
    }
}
