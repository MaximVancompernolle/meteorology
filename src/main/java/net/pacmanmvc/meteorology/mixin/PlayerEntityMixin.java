package net.pacmanmvc.meteorology.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.api.entity.PlayerEntityAccessor;
import net.pacmanmvc.meteorology.entity.projectile.AbstractBobberEntity;
import net.pacmanmvc.meteorology.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityAccessor {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public int experienceLevel;

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Unique
    public AbstractBobberEntity meteorology$fishingHook;

    @Override
    public void meteorology$setFishingHook(AbstractBobberEntity bobber) {
        this.meteorology$fishingHook = bobber;
    }

    @Override
    public AbstractBobberEntity meteorology$getFishingHook() {
        return this.meteorology$fishingHook;
    }

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    public void getNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.experienceLevel + 1);
    }

    @Inject(method = "updateTurtleHelmet", at = @At("TAIL"))
    private void updateTurtleHelmet(CallbackInfo ci) {
        ItemStack itemStack2 = this.getEquippedStack(EquipmentSlot.FEET);
        if (itemStack2.isOf(ModItems.FLIPPERS) && this.isSubmergedIn(FluidTags.WATER)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 100, 0, false, false, true));
        }
    }
}
