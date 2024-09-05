package net.pacmanmvc.meteorology.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.api.block.FluidBlockAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.Block.dropStacks;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {
    @Shadow
    @Final
    private static Direction[] DIRECTIONS;

    @Inject(method = "absorbWater(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void absorbWater(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Meteorology.LOGGER.info("Absorbing Water");
        int count = BlockPos.iterateRecursively(pos, 6, 512, (currentPos, queuer) -> {
            for (Direction direction : DIRECTIONS) {
                queuer.accept(currentPos.offset(direction));
            }
        }, currentPos -> {
            if (currentPos.equals(pos)) {
                return true;
            } else {
                BlockState blockState = world.getBlockState(currentPos);
                FluidState fluidState = world.getFluidState(currentPos);
                if (!fluidState.isIn(FluidTags.WATER)) {
                    return false;
                } else {
                    int distanceFromCenter = currentPos.getManhattanDistance(pos);
                    if (distanceFromCenter == 5 && blockState.getBlock() instanceof FluidDrainable && blockState.getBlock() instanceof FluidBlock block) {
                        ((FluidBlockAccessor) block).meteorology$setTickSponged(currentPos, world.getTime());
                        return false;
                    } else if (distanceFromCenter > 4) {
                        return false;
                    }
                    if (blockState.getBlock() instanceof FluidDrainable) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
                    }

                    if (blockState.getBlock() instanceof FluidBlock) {
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState());
                    } else {
                        if (!blockState.isOf(Blocks.KELP) && !blockState.isOf(Blocks.KELP_PLANT) && !blockState.isOf(Blocks.SEAGRASS) && !blockState.isOf(Blocks.TALL_SEAGRASS)) {
                            return false;
                        }

                        BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(currentPos) : null;
                        dropStacks(blockState, world, currentPos, blockEntity);
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    }

                    return true;
                }
            }
        });
        cir.setReturnValue(count > 1);
    }
}
