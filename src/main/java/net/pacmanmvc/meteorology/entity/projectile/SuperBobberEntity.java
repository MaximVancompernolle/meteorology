package net.pacmanmvc.meteorology.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.item.ModItems;

public class SuperBobberEntity extends FishingBobberEntity {
    public SuperBobberEntity(EntityType<? extends FishingBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super(type, world, luckBonus, waitTimeReductionTicks);
    }

    public SuperBobberEntity(EntityType<? extends FishingBobberEntity> entityType, World world) {
        super(entityType, world);
    }

    public SuperBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        super(thrower, world, luckBonus, waitTimeReductionTicks);
    }

    @Override
    public void tick() {
        super.tick();
    }

    private boolean removeIfInvalid(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        ItemStack itemStack2 = player.getOffHandStack();
        boolean bl = itemStack.isOf(ModItems.SUPER_ROD);
        boolean bl2 = itemStack2.isOf(ModItems.SUPER_ROD);
        if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 4096.0)) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }
}
