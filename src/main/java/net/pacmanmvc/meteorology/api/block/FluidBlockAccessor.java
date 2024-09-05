package net.pacmanmvc.meteorology.api.block;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public interface FluidBlockAccessor {

    HashMap<BlockPos, Long> spongeMap = new HashMap<>();

    long meteorology$getTickSponged(BlockPos pos);

    void meteorology$setTickSponged(BlockPos pos, long tickSponged);

    void meteorology$removeTickSponged(BlockPos pos);

}
