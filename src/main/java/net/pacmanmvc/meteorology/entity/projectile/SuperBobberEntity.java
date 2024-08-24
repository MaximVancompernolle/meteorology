package net.pacmanmvc.meteorology.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.entity.ModEntities;

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
}
