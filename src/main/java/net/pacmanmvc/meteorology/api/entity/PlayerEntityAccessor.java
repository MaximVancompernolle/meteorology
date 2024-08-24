package net.pacmanmvc.meteorology.api.entity;

import net.pacmanmvc.meteorology.entity.projectile.AbstractBobberEntity;

public interface PlayerEntityAccessor {

    void meteorology$setTestBobber(AbstractBobberEntity bobber);

    AbstractBobberEntity meteorology$getTestBobber();

}
