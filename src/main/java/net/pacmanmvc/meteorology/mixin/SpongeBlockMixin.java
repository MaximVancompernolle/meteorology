package net.pacmanmvc.meteorology.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
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
        int depth = 9;
        int iterations = 7000;
        int count = BlockPos.iterateRecursively(pos, depth, iterations, (currentPos, queuer) -> {
            for (Direction direction : DIRECTIONS) {
                queuer.accept(currentPos.offset(direction));
            }
        }, currentPos -> {
            if (currentPos.equals(pos)) {
                return true;
            } else {
                Meteorology.LOGGER.info("Checking block at " + currentPos);
                BlockState blockState = world.getBlockState(currentPos);
                FluidState fluidState = world.getFluidState(currentPos);
                if (!fluidState.isIn(FluidTags.WATER)) {
                    return false;
                } else {
                    int distanceFromCenter = currentPos.getManhattanDistance(pos);
                    if (currentPos.equals(new BlockPos(104, 25, -9))) {
                        Meteorology.LOGGER.info("HERE");
                    }
                    if (distanceFromCenter == depth && (blockState.getBlock() instanceof FluidDrainable || blockState.getBlock() instanceof FluidFillable) && blockState.getBlock() instanceof FluidBlock block) {
                        ((FluidBlockAccessor) block).meteorology$setTickSponged(currentPos, world.getTime());
                        return false;
                    } else if (distanceFromCenter > depth - 1) {
                        return false;
                    }
                    if (blockState.getBlock() instanceof FluidDrainable && blockState.getBlock() instanceof Waterloggable) {
                        if (blockState.get(Properties.WATERLOGGED)) {
                            world.setBlockState(currentPos, blockState.with(Properties.WATERLOGGED, Boolean.FALSE), Block.NOTIFY_ALL);
                            if (!blockState.canPlaceAt(world, currentPos)) {
                                world.breakBlock(currentPos, true);
                            }
                        }
                        return true;
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
