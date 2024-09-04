package net.pacmanmvc.meteorology.mixin;

import net.minecraft.block.FluidBlock;
import net.pacmanmvc.meteorology.api.block.FluidBlockAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FluidBlock.class)
public class FluidBlockMixin implements FluidBlockAccessor {

    @Unique
    private long meteorology$tickSponged = 0;

    @Override
    public long meteorology$getTickSponged() {
        return meteorology$tickSponged;
    }

    @Override
    public void meteorology$setTickSponged(long tickSponged) {
        this.meteorology$tickSponged = tickSponged;
    }
}
