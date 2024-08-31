package net.pacmanmvc.meteorology.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.entity.ModEntities;
import net.pacmanmvc.meteorology.item.ModItems;

public class GoodBobberEntity extends AbstractBobberEntity {

    public GoodBobberEntity(EntityType<? extends AbstractBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super(type, world, luckBonus, waitTimeReductionTicks);
    }

    public GoodBobberEntity(EntityType<? extends AbstractBobberEntity> entityType, World world) {
        super(entityType, world);
    }

    public GoodBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        super(ModEntities.GOOD_BOBBER, thrower, world, luckBonus, waitTimeReductionTicks);
    }

    @Override
    protected boolean removeIfInvalid(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        ItemStack itemStack2 = player.getOffHandStack();
        boolean bl = itemStack.isOf(ModItems.GOOD_ROD);
        boolean bl2 = itemStack2.isOf(ModItems.GOOD_ROD);
        if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 2048.0)) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }

    @Override
    protected int advanceFishingTicks(BlockPos pos) {
        int i = 1;

        if (this.random.nextFloat() < 0.5F && this.getWorld().hasRain(pos)) {
            i++;
        }

        if (this.random.nextFloat() < 0.5F && !this.getWorld().isSkyVisible(pos)) {
            i++;
        }

        return i;
    }

    @Override
    public double getSuperPercentage() {
        return 0.9;
    }

    @Override
    public double getGoodPercentage() {
        return 0.75;
    }
}
