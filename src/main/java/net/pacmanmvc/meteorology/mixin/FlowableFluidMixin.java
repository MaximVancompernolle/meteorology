package net.pacmanmvc.meteorology.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.api.block.FluidBlockAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {

    @Inject(method = "canFlow", at = @At("HEAD"), cancellable = true)
    private void canFlow(
            BlockView world,
            BlockPos fluidPos,
            BlockState fluidBlockState,
            Direction flowDirection,
            BlockPos flowTo,
            BlockState flowToBlockState,
            FluidState fluidState,
            Fluid fluid,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if(fluidBlockState.getBlock() instanceof FluidDrainable && fluidBlockState.getBlock() instanceof FluidBlock block) {
            long tickSponged = ((FluidBlockAccessor) block).meteorology$getTickSponged(fluidPos);
            World worldWorld = (World) world;
            long timeDiff = worldWorld.getTime() - tickSponged;
            if (timeDiff <= Meteorology.SPONGE_HOLD_TIME_SECONDS * 20) {
                if (!worldWorld.getFluidTickScheduler().isQueued(fluidPos, fluid)) {
                    worldWorld.scheduleFluidTick(fluidPos, fluid, (int) (Meteorology.SPONGE_HOLD_TIME_SECONDS * 20 - timeDiff));
                }
                cir.setReturnValue(false);
            } else if (tickSponged != 0) {
                ((FluidBlockAccessor) block).meteorology$removeTickSponged(fluidPos);
            }
        }

    }
}
