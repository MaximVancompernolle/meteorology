package net.pacmanmvc.meteorology.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EggEntity.class)
public abstract class EggEntityMixin extends ThrownItemEntity {
    public EggEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity entity = entityHitResult.getEntity();
        if (entity.getType().equals(EntityType.ZOMBIE)) {
            ZombieEntity zombieEntity = (ZombieEntity) entity;
            PiglinEntity piglinEntity = EntityType.PIGLIN.create(this.getWorld());
            if (piglinEntity != null) {
                piglinEntity.equipStack(EquipmentSlot.MAINHAND,
                        piglinEntity.getRandom().nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD));
                piglinEntity.refreshPositionAndAngles(zombieEntity.getX(), zombieEntity.getY(), zombieEntity.getZ(), zombieEntity.getYaw(), zombieEntity.getPitch());
                piglinEntity.setAiDisabled(zombieEntity.isAiDisabled());
                piglinEntity.setBaby(zombieEntity.isBaby());
                if (zombieEntity.hasCustomName()) {
                    piglinEntity.setCustomName(zombieEntity.getCustomName());
                    piglinEntity.setCustomNameVisible(zombieEntity.isCustomNameVisible());
                }
                piglinEntity.setPersistent();
                this.getWorld().spawnEntity(piglinEntity);
                zombieEntity.discard();
            }
        }
    }

    @Override
    public Item getDefaultItem() {
        return Items.EGG;
    }
}
