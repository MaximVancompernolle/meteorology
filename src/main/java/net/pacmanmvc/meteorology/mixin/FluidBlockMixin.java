package net.pacmanmvc.meteorology.mixin;

import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.pacmanmvc.meteorology.api.block.FluidBlockAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidBlock.class)
public class FluidBlockMixin implements FluidBlockAccessor {

    @Override
    public long meteorology$getTickSponged(BlockPos pos) {
        return spongeMap.getOrDefault(pos, 0L);
    }

    @Override
    public void meteorology$setTickSponged(BlockPos pos, long tickSponged) {
        spongeMap.put(pos, tickSponged);
    }

    @Override
    public void meteorology$removeTickSponged(BlockPos pos) {
        spongeMap.remove(pos);
    }
}
