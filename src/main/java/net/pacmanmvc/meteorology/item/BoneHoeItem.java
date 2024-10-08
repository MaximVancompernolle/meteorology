package net.pacmanmvc.meteorology.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoneHoeItem extends HoeItem {
    public BoneHoeItem(Settings settings) {
        super(ModToolMaterial.BONE, settings);
    }

    private static boolean useOnFertilizable(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof Fertilizable fertilizable && fertilizable.isFertilizable(world, pos, blockState)) {
            if (world instanceof ServerWorld) {
                if (fertilizable.canGrow(world, world.random, pos, blockState)) {
                    fertilizable.grow((ServerWorld)world, world.random, pos, blockState);
                }
            }
            return true;
        }
        return false;
    }

    private static boolean useOnGround(World world, BlockPos blockPos, @Nullable Direction facing) {
        if (world.getBlockState(blockPos).isOf(Blocks.WATER) && world.getFluidState(blockPos).getLevel() == 8) {
            if (world instanceof ServerWorld) {
                Random random = world.getRandom();

                label78:
                for (int i = 0; i < 128; i++) {
                    BlockPos blockPos2 = blockPos;
                    BlockState blockState = Blocks.SEAGRASS.getDefaultState();

                    for (int j = 0; j < i / 16; j++) {
                        blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                        if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                            continue label78;
                        }
                    }

                    RegistryEntry<Biome> registryEntry = world.getBiome(blockPos2);
                    if (registryEntry.isIn(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                        if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                            blockState = Registries.BLOCK
                                    .getRandomEntry(BlockTags.WALL_CORALS, world.random)
                                    .map(blockEntry -> blockEntry.value().getDefaultState())
                                    .orElse(blockState);
                            if (blockState.contains(DeadCoralWallFanBlock.FACING)) {
                                blockState = blockState.with(DeadCoralWallFanBlock.FACING, facing);
                            }
                        } else if (random.nextInt(4) == 0) {
                            blockState = Registries.BLOCK
                                    .getRandomEntry(BlockTags.UNDERWATER_BONEMEALS, world.random)
                                    .map(blockEntry -> blockEntry.value().getDefaultState())
                                    .orElse(blockState);
                        }
                    }

                    if (blockState.isIn(BlockTags.WALL_CORALS, state -> state.contains(DeadCoralWallFanBlock.FACING))) {
                        for (int k = 0; !blockState.canPlaceAt(world, blockPos2) && k < 4; k++) {
                            blockState = blockState.with(DeadCoralWallFanBlock.FACING, Direction.Type.HORIZONTAL.random(random));
                        }
                    }

                    if (blockState.canPlaceAt(world, blockPos2)) {
                        BlockState blockState2 = world.getBlockState(blockPos2);
                        if (blockState2.isOf(Blocks.WATER) && world.getFluidState(blockPos2).getLevel() == 8) {
                            world.setBlockState(blockPos2, blockState, Block.NOTIFY_ALL);
                        } else if (blockState2.isOf(Blocks.SEAGRASS) && random.nextInt(10) == 0) {
                            ((Fertilizable) Blocks.SEAGRASS).grow((ServerWorld) world, random, blockPos2, blockState2);
                        }
                    }
                }

            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (context.getPlayer() == null) {
            return ActionResult.FAIL;
        }
        BlockPos blockPos = context.getBlockPos();
        Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>> pair = TILLING_ACTIONS.get(
                world.getBlockState(blockPos).getBlock()
        );
        if (pair == null) {
            BlockPos blockPos2 = blockPos.offset(context.getSide());
            if (useOnFertilizable(world, blockPos)) {
                if (!world.isClient) {
                    context.getPlayer().emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                    world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
                    context.getStack().damage(2, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
                }

                return ActionResult.success(world.isClient);
            } else {
                BlockState blockState = world.getBlockState(blockPos);
                boolean bl = blockState.isSideSolidFullSquare(world, blockPos, context.getSide());
                if (bl && useOnGround(world, blockPos2, context.getSide())) {
                    if (!world.isClient) {
                        context.getPlayer().emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                        world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos2, 15);
                        context.getStack().damage(2, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
                    }

                    return ActionResult.success(world.isClient);
                } else {
                    return ActionResult.PASS;
                }
            }
        } else {
            Predicate<ItemUsageContext> predicate = pair.getFirst();
            Consumer<ItemUsageContext> consumer = pair.getSecond();
            if (predicate.test(context)) {
                PlayerEntity playerEntity = context.getPlayer();
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!world.isClient) {
                    consumer.accept(context);
                    if (playerEntity != null) {
                        context.getStack().damage(1, playerEntity, LivingEntity.getSlotForHand(context.getHand()));
                    }
                }

                return ActionResult.success(world.isClient);
            } else {
                return ActionResult.PASS;
            }
        }
    }
}
