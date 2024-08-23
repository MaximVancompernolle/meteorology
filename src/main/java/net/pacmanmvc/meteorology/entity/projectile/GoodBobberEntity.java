package net.pacmanmvc.meteorology.entity.projectile;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GoodBobberEntity extends FishingBobberEntity {
    private final Random velocityRandom = Random.create();
    private boolean caughtFish;
    private int outOfOpenWaterTicks;
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(GoodBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CAUGHT_FISH = DataTracker.registerData(GoodBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int removalTimer;
    private int hookCountdown;
    private int waitCountdown;
    private int fishTravelCountdown;
    private float fishAngle;
    private boolean inOpenWater = true;
    @Nullable
    private Entity hookedEntity;
    private GoodBobberEntity.State state = State.FLYING;
    private final int luckBonus;
    private final int waitTimeReductionTicks;

    public GoodBobberEntity(EntityType<? extends GoodBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super(type, world, luckBonus, waitTimeReductionTicks);
        this.luckBonus = luckBonus;
        this.waitTimeReductionTicks = waitTimeReductionTicks;
    }

    public GoodBobberEntity(EntityType<? extends GoodBobberEntity> entityType, World world) {
        this(entityType, world, 0, 0);
    }

    public GoodBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        super(thrower, world, luckBonus, waitTimeReductionTicks);
        this.luckBonus = luckBonus;
        this.waitTimeReductionTicks = waitTimeReductionTicks;
        this.setOwner(thrower);
    }

    @Override
    public void tick() {
        this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.getWorld().getTime());
//        super.tick();
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity == null) {
            this.discard();
        } else if (this.getWorld().isClient || this.removeIfInvalid(playerEntity)) {
            if (this.isOnGround()) {
                this.removalTimer++;
                if (this.removalTimer >= 1200) {
                    this.discard();
                    return;
                }
            } else {
                this.removalTimer = 0;
            }

            float f = 0.0F;
            BlockPos blockPos = this.getBlockPos();
            FluidState fluidState = this.getWorld().getFluidState(blockPos);
            if (fluidState.isIn(FluidTags.WATER)) {
                f = fluidState.getHeight(this.getWorld(), blockPos);
            }

            boolean bl = f > 0.0F;
            if (this.state == GoodBobberEntity.State.FLYING) {
                if (this.hookedEntity != null) {
                    this.setVelocity(Vec3d.ZERO);
                    this.state = GoodBobberEntity.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (bl) {
                    this.setVelocity(this.getVelocity().multiply(0.3, 0.2, 0.3));
                    this.state = GoodBobberEntity.State.BOBBING;
                    return;
                }

                this.checkForCollision();
            } else {
                if (this.state == GoodBobberEntity.State.HOOKED_IN_ENTITY) {
                    if (this.hookedEntity != null) {
                        if (!this.hookedEntity.isRemoved() && this.hookedEntity.getWorld().getRegistryKey() == this.getWorld().getRegistryKey()) {
                            this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8), this.hookedEntity.getZ());
                        } else {
                            this.updateHookedEntityId(null);
                            this.state = GoodBobberEntity.State.FLYING;
                        }
                    }

                    return;
                }

                if (this.state == GoodBobberEntity.State.BOBBING) {
                    Vec3d vec3d = this.getVelocity();
                    double d = this.getY() + vec3d.y - (double)blockPos.getY() - (double)f;
                    if (Math.abs(d) < 0.01) {
                        d += Math.signum(d) * 0.1;
                    }

                    this.setVelocity(vec3d.x * 0.9, vec3d.y - d * (double)this.random.nextFloat() * 0.2, vec3d.z * 0.9);
                    if (this.hookCountdown <= 0 && this.fishTravelCountdown <= 0) {
                        this.inOpenWater = true;
                    } else {
                        this.inOpenWater = this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(blockPos);
                    }

                    if (bl) {
                        this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
                        if (this.caughtFish) {
                            this.setVelocity(this.getVelocity().add(0.0, -0.1 * (double)this.velocityRandom.nextFloat() * (double)this.velocityRandom.nextFloat(), 0.0));
                        }

                        if (!this.getWorld().isClient) {
                            this.tickFishingLogic(blockPos);
                        }
                    } else {
                        this.outOfOpenWaterTicks = Math.min(10, this.outOfOpenWaterTicks + 1);
                    }
                }
            }

            if (!fluidState.isIn(FluidTags.WATER)) {
                this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
            }

            this.move(MovementType.SELF, this.getVelocity());
            this.updateRotation();
            if (this.state == GoodBobberEntity.State.FLYING && (this.isOnGround() || this.horizontalCollision)) {
                this.setVelocity(Vec3d.ZERO);
            }

            this.setVelocity(this.getVelocity().multiply(0.92));
            this.refreshPosition();
        }
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.setPlayerFishHook(this);
    }

    private void setPlayerFishHook(@Nullable GoodBobberEntity goodBobber) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity != null) {
            playerEntity.fishHook = goodBobber;
        }
    }

    @Override
    public int use(ItemStack usedItem) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (!this.getWorld().isClient && playerEntity != null && this.removeIfInvalid(playerEntity)) {
            int i = 0;
            if (this.hookedEntity != null) {
                this.pullHookedEntity(this.hookedEntity);
                Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) playerEntity, usedItem, this, Collections.emptyList());
                this.getWorld().sendEntityStatus(this, EntityStatuses.PULL_HOOKED_ENTITY);
                i = this.hookedEntity instanceof ItemEntity ? 2 : 4;
            } else if (this.hookCountdown > 0) {
                LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder((ServerWorld) this.getWorld())
                        .add(LootContextParameters.ORIGIN, this.getPos())
                        .add(LootContextParameters.TOOL, usedItem)
                        .add(LootContextParameters.THIS_ENTITY, this)
                        .luck((float) this.luckBonus + playerEntity.getLuck())
                        .build(LootContextTypes.FISHING);
                LootTable lootTable = this.getWorld().getServer().getReloadableRegistries().getLootTable(LootTables.FISHING_GAMEPLAY);
                List<ItemStack> list = lootTable.generateLoot(lootContextParameterSet);
                Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) playerEntity, usedItem, this, list);

                for (ItemStack itemStack : list) {
                    ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), itemStack);
                    double d = playerEntity.getX() - this.getX();
                    double e = playerEntity.getY() - this.getY();
                    double f = playerEntity.getZ() - this.getZ();
                    itemEntity.setVelocity(d * 0.2, e * 0.2 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.2);
                    this.getWorld().spawnEntity(itemEntity);
                    playerEntity.getWorld()
                            .spawnEntity(
                                    new ExperienceOrbEntity(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, this.random.nextInt(7) + 1)
                            );
                    if (itemStack.isIn(ItemTags.FISHES)) {
                        playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }

            if (this.isOnGround()) {
                i = 2;
            }

            this.discard();
            return i;
        } else {
            return 0;
        }
    }

    private boolean removeIfInvalid(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        ItemStack itemStack2 = player.getOffHandStack();
        boolean bl = itemStack.isOf(ModItems.GOOD_ROD);
        boolean bl2 = itemStack2.isOf(ModItems.GOOD_ROD);
        if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 2034.0)) {
            return true;
        } else {
            this.discard();
            return false;
        }
    }

    private void checkForCollision() {
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.hitOrDeflect(hitResult);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!this.getWorld().isClient) {
            this.updateHookedEntityId(entityHitResult.getEntity());
        }
    }

    private void updateHookedEntityId(@Nullable Entity entity) {
        this.hookedEntity = entity;
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    private void tickFishingLogic(BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld)this.getWorld();
        int i = 1;
        BlockPos blockPos = pos.up();
        if (this.random.nextFloat() < 0.5F && this.getWorld().hasRain(blockPos)) {
            i++;
        }

        if (this.random.nextFloat() < 0.5F && !this.getWorld().isSkyVisible(blockPos)) {
            i++;
        }

        if (this.hookCountdown > 0) {
            this.hookCountdown--;
            if (this.hookCountdown <= 0) {
                this.waitCountdown = 0;
                this.fishTravelCountdown = 0;
                this.getDataTracker().set(CAUGHT_FISH, false);
            }
        } else if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
                this.fishAngle = this.fishAngle + (float)this.random.nextTriangular(0.0, 9.188);
                float f = this.fishAngle * (float) (Math.PI / 180.0);
                float g = MathHelper.sin(f);
                float h = MathHelper.cos(f);
                double d = this.getX() + (double)(g * (float)this.fishTravelCountdown * 0.1F);
                double e = (float)MathHelper.floor(this.getY()) + 1.0F;
                double j = this.getZ() + (double)(h * (float)this.fishTravelCountdown * 0.1F);
                BlockState blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
                if (blockState.isOf(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15F) {
                        serverWorld.spawnParticles(ParticleTypes.BUBBLE, d, e - 0.1F, j, 1, g, 0.1, h, 0.0);
                    }

                    float k = g * 0.04F;
                    float l = h * 0.04F;
                    serverWorld.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, l, 0.01, -k, 1.0);
                    serverWorld.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, -l, 0.01, k, 1.0);
                }
            } else {
                this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double m = this.getY() + 0.5;
                serverWorld.spawnParticles(
                        ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), this.getWidth(), 0.0, this.getWidth(), 0.2F
                );
                serverWorld.spawnParticles(
                        ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), this.getWidth(), 0.0, this.getWidth(), 0.2F
                );
                this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
                this.getDataTracker().set(CAUGHT_FISH, true);
            }
        } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            float f = 0.15F;
            if (this.waitCountdown < 20) {
                f += (float)(20 - this.waitCountdown) * 0.05F;
            } else if (this.waitCountdown < 40) {
                f += (float)(40 - this.waitCountdown) * 0.02F;
            } else if (this.waitCountdown < 60) {
                f += (float)(60 - this.waitCountdown) * 0.01F;
            }

            if (this.random.nextFloat() < f) {
                float g = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float h = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
                double d = this.getX() + (double)(MathHelper.sin(g) * h) * 0.1;
                double e = (float)MathHelper.floor(this.getY()) + 1.0F;
                double j = this.getZ() + (double)(MathHelper.cos(g) * h) * 0.1;
                BlockState blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
                if (blockState.isOf(Blocks.WATER)) {
                    serverWorld.spawnParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
                }
            }

            if (this.waitCountdown <= 0) {
                this.fishAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
                this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
        } else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            this.waitCountdown = this.waitCountdown - this.waitTimeReductionTicks;
        }
    }

    private GoodBobberEntity.PositionType getPositionType(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end)
                .map(this::getPositionType)
                .reduce((positionType, positionType2) -> positionType == positionType2 ? positionType : PositionType.INVALID)
                .orElse(PositionType.INVALID);
    }

    private GoodBobberEntity.PositionType getPositionType(BlockPos pos) {
        BlockState blockState = this.getWorld().getBlockState(pos);
        if (!blockState.isAir() && !blockState.isOf(Blocks.LILY_PAD)) {
            FluidState fluidState = blockState.getFluidState();
            return fluidState.isIn(FluidTags.WATER) && fluidState.isStill() && blockState.getCollisionShape(this.getWorld(), pos).isEmpty()
                    ? GoodBobberEntity.PositionType.INSIDE_WATER
                    : GoodBobberEntity.PositionType.INVALID;
        } else {
            return GoodBobberEntity.PositionType.ABOVE_WATER;
        }
    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        GoodBobberEntity.PositionType positionType = GoodBobberEntity.PositionType.INVALID;

        for (int i = -1; i <= 1; i++) {
            GoodBobberEntity.PositionType positionType2 = this.getPositionType(pos.add(-1, i, -1), pos.add(1, i, 1));
            switch (positionType2) {
                case ABOVE_WATER:
                    if (positionType == GoodBobberEntity.PositionType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (positionType == GoodBobberEntity.PositionType.ABOVE_WATER) {
                        return false;
                    }
                    break;
                case INVALID:
                    return false;
            }

            positionType = positionType2;
        }

        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HOOK_ENTITY_ID, 0);
        builder.add(CAUGHT_FISH, false);
    }


    static enum PositionType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;
    }

    static enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }
}
