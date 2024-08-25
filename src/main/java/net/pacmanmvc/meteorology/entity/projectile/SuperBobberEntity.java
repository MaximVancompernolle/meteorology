package net.pacmanmvc.meteorology.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.entity.ModEntities;
import net.pacmanmvc.meteorology.item.ModItems;

public class SuperBobberEntity extends AbstractBobberEntity {

    public SuperBobberEntity(EntityType<? extends AbstractBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super(type, world, luckBonus, waitTimeReductionTicks);
    }

    public SuperBobberEntity(EntityType<? extends AbstractBobberEntity> entityType, World world) {
        super(entityType, world);
    }

    public SuperBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        super(ModEntities.SUPER_BOBBER, thrower, world, luckBonus, waitTimeReductionTicks);
    }

    @Override
    protected boolean removeIfInvalid(PlayerEntity player) {
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

    @Override
    protected int advanceFishingTicks(BlockPos pos) {
        int i = 1;

        if (this.getWorld().hasRain(pos)) {
            i++;
        }

        if (!this.getWorld().isSkyVisible(pos)) {
            i++;
        }

        return i;
    }
}
