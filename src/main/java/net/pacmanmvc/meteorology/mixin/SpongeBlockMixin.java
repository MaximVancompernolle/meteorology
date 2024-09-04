package net.pacmanmvc.meteorology.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.api.block.FluidBlockAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.block.Block.dropStacks;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {
    @Shadow @Final private static Direction[] DIRECTIONS;

    @Unique
    private static BlockPos[] getSphereOutline(BlockPos pos, int radius) {
        List<BlockPos> sphere = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x * x + y * y + z * z - radius * radius) < radius) {
                        sphere.add(pos.add(x, y, z));
                    }
                }
            }
        }
        return sphere.toArray(new BlockPos[0]);
    }

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
                    if (blockState.getBlock() instanceof FluidDrainable fluidDrainable && !fluidDrainable.tryDrainFluid(null, world, currentPos, blockState).isEmpty()) {
                        return true;
                    }

                    if (blockState.getBlock() instanceof FluidBlock) {
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
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
        if (count > 1) {
            BlockPos[] sphere = getSphereOutline(pos, 4);
            for (BlockPos currentPos : sphere) {
                BlockState blockState = world.getBlockState(currentPos);
                if (blockState.getBlock() instanceof FluidDrainable && blockState.getBlock() instanceof FluidBlock block && blockState.getFluidState().isIn(FluidTags.WATER)) {
                    ((FluidBlockAccessor) block).meteorology$setTickSponged(world.getTime());
                    world.scheduleFluidTick(currentPos, blockState.getFluidState().getFluid(), Meteorology.SPONGE_HOLD_TIME_SECONDS * 20 + 5, TickPriority.HIGH);
                }
            }
        }
         cir.setReturnValue(count > 1);
    }
}
