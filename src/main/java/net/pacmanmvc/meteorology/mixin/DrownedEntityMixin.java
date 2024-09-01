package net.pacmanmvc.meteorology.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.item.ModItems;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin extends ZombieEntity implements RangedAttackMob {
    @Shadow public abstract EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData);

    public DrownedEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initialize", at = @At("HEAD"), cancellable = true)
    public void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        if (this.getEquippedStack(EquipmentSlot.OFFHAND).isEmpty() && world.getRandom().nextFloat() < 0.06F) {
            this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
            this.updateDropChances(EquipmentSlot.OFFHAND);
        }

        cir.setReturnValue(entityData);
    }

    @Inject(method = "initEquipment", at = @At("HEAD"), cancellable = true)
    public void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        if (random.nextFloat() > 0.8F) {
            int i = random.nextInt(16);
            if (i < 10) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            } else if (i < 13) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            } else if (i < 15) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.GOOD_ROD));
            } else {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SUPER_ROD));
            }
        }

        ci.cancel();
    }
}
