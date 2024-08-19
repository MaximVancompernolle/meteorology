package net.pacmanmvc.meteorology.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.item.ModItems;

public class GoodBobberEntity extends FishingBobberEntity {
    public GoodBobberEntity(EntityType<? extends FishingBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super(type, world, luckBonus, waitTimeReductionTicks);
    }

    public GoodBobberEntity(EntityType<? extends FishingBobberEntity> entityType, World world) {
        super(entityType, world);
    }

    public GoodBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        super(thrower, world, luckBonus, waitTimeReductionTicks);
    }

    private boolean removeIfInvalid(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        ItemStack itemStack2 = player.getOffHandStack();
        boolean bl = itemStack.isOf(ModItems.GOOD_ROD);
        boolean bl2 = itemStack2.isOf(ModItems.GOOD_ROD);
        if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 2034.0)) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }
}
