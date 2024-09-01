package net.pacmanmvc.meteorology.entity.projectile;

import com.mojang.logging.LogUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pacmanmvc.meteorology.api.entity.PlayerEntityAccessor;
import net.pacmanmvc.meteorology.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

import static net.pacmanmvc.meteorology.loot.ModLootTables.*;

public abstract class AbstractBobberEntity extends ProjectileEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Random velocityRandom = Random.create();
    private boolean caughtFish;
    private int outOfOpenWaterTicks;
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(AbstractBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CAUGHT_FISH = DataTracker.registerData(AbstractBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int removalTimer;
    private int hookCountdown;
    private int maxHookCountdown;
    private int waitCountdown;
    private int fishTravelCountdown;
    private float fishAngle;
    private boolean inOpenWater = true;
    @Nullable
    private Entity hookedEntity;
    private AbstractBobberEntity.State state = AbstractBobberEntity.State.FLYING;
    private final int luckBonus;
    private final int waitTimeReductionTicks;
    private boolean showExclamationMark = false;

    public AbstractBobberEntity(EntityType<? extends AbstractBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super(type, world);
        this.ignoreCameraFrustum = true;
        this.luckBonus = Math.max(0, luckBonus);
        this.waitTimeReductionTicks = Math.max(0, waitTimeReductionTicks);
    }

    public AbstractBobberEntity(EntityType<? extends AbstractBobberEntity> entityType, World world) {
        this(entityType, world, 0, 0);
    }

    public AbstractBobberEntity(EntityType<? extends AbstractBobberEntity> type, PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        this(type, world, luckBonus, waitTimeReductionTicks);
        this.setOwner(thrower);
        float f = thrower.getPitch();
        float g = thrower.getYaw();
        float h = MathHelper.cos(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
        float i = MathHelper.sin(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
        float j = -MathHelper.cos(-f * (float) (Math.PI / 180.0));
        float k = MathHelper.sin(-f * (float) (Math.PI / 180.0));
        double d = thrower.getX() - (double) i * 0.3;
        double e = thrower.getEyeY();
        double l = thrower.getZ() - (double) h * 0.3;
        this.refreshPositionAndAngles(d, e, l, g, f);
        Vec3d vec3d = new Vec3d(-i, MathHelper.clamp(-(k / j), -5.0F, 5.0F), -h);
        double m = vec3d.length();
        vec3d = vec3d.multiply(
                0.6 / m + this.random.nextTriangular(0.5, 0.0103365),
                0.6 / m + this.random.nextTriangular(0.5, 0.0103365),
                0.6 / m + this.random.nextTriangular(0.5, 0.0103365)
        );
        this.setVelocity(vec3d);
        this.setYaw((float) (MathHelper.atan2(vec3d.x, vec3d.z) * 180.0F / (float) Math.PI));
        this.setPitch((float) (MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 180.0F / (float) Math.PI));
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    public double getHookCountdownPercentage() {
        return (double) this.hookCountdown / this.maxHookCountdown;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HOOK_ENTITY_ID, 0);
        builder.add(CAUGHT_FISH, false);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (HOOK_ENTITY_ID.equals(data)) {
            int i = this.getDataTracker().get(HOOK_ENTITY_ID);
            this.hookedEntity = i > 0 ? this.getWorld().getEntityById(i - 1) : null;
        }

        if (CAUGHT_FISH.equals(data)) {
            this.caughtFish = this.getDataTracker().get(CAUGHT_FISH);
            if (this.caughtFish) {
                this.setVelocity(this.getVelocity().x, -0.4F * MathHelper.nextFloat(this.velocityRandom, 0.6F, 1.0F), this.getVelocity().z);
            }
        }

        super.onTrackedDataSet(data);
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 4096.0;
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
    }

    @Override
    public void tick() {
        this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.getWorld().getTime());
        super.tick();
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity == null) {
            this.discard();
        } else if (this.getWorld().isClient || !this.removeIfInvalid(playerEntity)) {
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
            if (this.state == AbstractBobberEntity.State.FLYING) {
                if (this.hookedEntity != null) {
                    this.setVelocity(Vec3d.ZERO);
                    this.state = AbstractBobberEntity.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (bl) {
                    this.setVelocity(this.getVelocity().multiply(0.3, 0.2, 0.3));
                    this.state = AbstractBobberEntity.State.BOBBING;
                    return;
                }

                this.checkForCollision();
            } else {
                if (this.state == AbstractBobberEntity.State.HOOKED_IN_ENTITY) {
                    if (this.hookedEntity != null) {
                        if (!this.hookedEntity.isRemoved() && this.hookedEntity.getWorld().getRegistryKey() == this.getWorld().getRegistryKey()) {
                            this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8), this.hookedEntity.getZ());
                        } else {
                            this.updateHookedEntityId(null);
                            this.state = AbstractBobberEntity.State.FLYING;
                        }
                    }

                    return;
                }

                if (this.state == AbstractBobberEntity.State.BOBBING) {
                    Vec3d vec3d = this.getVelocity();
                    double d = this.getY() + vec3d.y - (double) blockPos.getY() - (double) f;
                    if (Math.abs(d) < 0.01) {
                        d += Math.signum(d) * 0.1;
                    }

                    this.setVelocity(vec3d.x * 0.9, vec3d.y - d * (double) this.random.nextFloat() * 0.2, vec3d.z * 0.9);
                    if (this.hookCountdown <= 0 && this.fishTravelCountdown <= 0) {
                        this.inOpenWater = true;
                    } else {
                        this.inOpenWater = this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(blockPos);
                    }

                    if (bl) {
                        this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
                        if (this.caughtFish) {
                            this.setVelocity(this.getVelocity().add(0.0, -0.1 * (double) this.velocityRandom.nextFloat() * (double) this.velocityRandom.nextFloat(), 0.0));
                            this.setShowExclamationMark(true);
                            if (hookCountdown <= 0) {
                                this.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                                this.hookCountdown = MathHelper.nextInt(this.random, 20, 60);
                                this.maxHookCountdown = hookCountdown;
                            }
                        } else {
                            this.setShowExclamationMark(false);
                        }

                        if (this.hookCountdown > 0) {
                            this.hookCountdown--;
                            if (this.hookCountdown <= 0) {
                                this.waitCountdown = 0;
                                this.fishTravelCountdown = 0;
                                this.getDataTracker().set(CAUGHT_FISH, false);
                            }
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
            if (this.state == AbstractBobberEntity.State.FLYING && (this.isOnGround() || this.horizontalCollision)) {
                this.setVelocity(Vec3d.ZERO);
            }

            this.setVelocity(this.getVelocity().multiply(0.92));
            this.refreshPosition();
        }
    }

    public abstract double getSuperPercentage();

    public abstract double getGoodPercentage();

    protected boolean removeIfInvalid(PlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        ItemStack itemStack2 = player.getOffHandStack();
        boolean bl = itemStack.isIn(ModTags.Items.FISHING_RODS);
        boolean bl2 = itemStack2.isIn(ModTags.Items.FISHING_RODS);
        if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 1024.0)) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }

    private void checkForCollision() {
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.hitOrDeflect(hitResult);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient) {
            this.updateHookedEntityId(entityHitResult.getEntity());
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
    }

    private void updateHookedEntityId(@Nullable Entity entity) {
        this.hookedEntity = entity;
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    protected int advanceFishingTicks(BlockPos pos) {
        int i = 1;

        if (this.random.nextFloat() < 0.25F && this.getWorld().hasRain(pos)) {
            i++;
        }

        if (this.random.nextFloat() < 0.5F && !this.getWorld().isSkyVisible(pos)) {
            i--;
        }

        return i;
    }

    private void tickFishingLogic(BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        BlockPos blockPos = pos.up();
        int i = advanceFishingTicks(blockPos);

        if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
                this.fishAngle = this.fishAngle + (float) this.random.nextTriangular(0.0, 9.188);
                float f = this.fishAngle * (float) (Math.PI / 180.0);
                float g = MathHelper.sin(f);
                float h = MathHelper.cos(f);
                double d = this.getX() + (double) (g * (float) this.fishTravelCountdown * 0.1F);
                double e = (float) MathHelper.floor(this.getY()) + 1.0F;
                double j = this.getZ() + (double) (h * (float) this.fishTravelCountdown * 0.1F);
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
                        ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int) (1.0F + this.getWidth() * 20.0F), this.getWidth(), 0.0, this.getWidth(), 0.2F
                );
                serverWorld.spawnParticles(
                        ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int) (1.0F + this.getWidth() * 20.0F), this.getWidth(), 0.0, this.getWidth(), 0.2F
                );
                this.getDataTracker().set(CAUGHT_FISH, true);
            }
        } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            float f = 0.15F;
            if (this.waitCountdown < 20) {
                f += (float) (20 - this.waitCountdown) * 0.05F;
            } else if (this.waitCountdown < 40) {
                f += (float) (40 - this.waitCountdown) * 0.02F;
            } else if (this.waitCountdown < 60) {
                f += (float) (60 - this.waitCountdown) * 0.01F;
            }

            if (this.random.nextFloat() < f) {
                float g = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float h = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
                double d = this.getX() + (double) (MathHelper.sin(g) * h) * 0.1;
                double e = (float) MathHelper.floor(this.getY()) + 1.0F;
                double j = this.getZ() + (double) (MathHelper.cos(g) * h) * 0.1;
                BlockState blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
                if (blockState.isOf(Blocks.WATER)) {
                    serverWorld.spawnParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
                }
            }

            if (this.waitCountdown <= 0) {
                this.fishAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
                this.fishTravelCountdown = MathHelper.nextInt(this.random, 10, 40);
            }
        } else {
            this.waitCountdown = MathHelper.nextInt(this.random, Math.max(0, 100 - this.waitTimeReductionTicks), 600 - this.waitTimeReductionTicks);
        }
    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        AbstractBobberEntity.PositionType positionType = AbstractBobberEntity.PositionType.INVALID;

        for (int i = -1; i <= 2; i++) {
            AbstractBobberEntity.PositionType positionType2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2));
            switch (positionType2) {
                case ABOVE_WATER:
                    if (positionType == AbstractBobberEntity.PositionType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (positionType == AbstractBobberEntity.PositionType.ABOVE_WATER) {
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

    private AbstractBobberEntity.PositionType getPositionType(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end)
                .map(this::getPositionType)
                .reduce((positionType, positionType2) -> positionType == positionType2 ? positionType : PositionType.INVALID)
                .orElse(PositionType.INVALID);
    }

    private AbstractBobberEntity.PositionType getPositionType(BlockPos pos) {
        BlockState blockState = this.getWorld().getBlockState(pos);
        if (!blockState.isAir() && !blockState.isOf(Blocks.LILY_PAD)) {
            FluidState fluidState = blockState.getFluidState();
            return fluidState.isIn(FluidTags.WATER) && fluidState.isStill() && blockState.getCollisionShape(this.getWorld(), pos).isEmpty()
                    ? AbstractBobberEntity.PositionType.INSIDE_WATER
                    : AbstractBobberEntity.PositionType.INVALID;
        } else {
            return AbstractBobberEntity.PositionType.ABOVE_WATER;
        }
    }

    public boolean isInOpenWater() {
        return this.inOpenWater;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
    }

    public int use(ItemStack usedItem) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (!this.getWorld().isClient && playerEntity != null && !this.removeIfInvalid(playerEntity)) {
            this.setShowExclamationMark(true);
            int i = 0;
            if (this.hookedEntity != null) {
                this.pullHookedEntity(this.hookedEntity);
//                Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this, Collections.emptyList());
                this.getWorld().sendEntityStatus(this, EntityStatuses.PULL_HOOKED_ENTITY);
                i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
            } else if (this.hookCountdown > 0) {
                LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder((ServerWorld) this.getWorld())
                        .add(LootContextParameters.ORIGIN, this.getPos())
                        .add(LootContextParameters.TOOL, usedItem)
                        .add(LootContextParameters.THIS_ENTITY, this)
                        .luck((float) this.luckBonus + playerEntity.getLuck())
                        .build(LootContextTypes.FISHING);
                LootTable lootTable;
                double exclamationTicks = this.getHookCountdownPercentage();

                if (exclamationTicks > this.getSuperPercentage()) {
                    lootTable = this.getWorld().getServer().getReloadableRegistries().getLootTable(SUPER_FISHING_LOOT_TABLE);
                } else if (exclamationTicks > this.getGoodPercentage()) {
                    lootTable = this.getWorld().getServer().getReloadableRegistries().getLootTable(GOOD_FISHING_LOOT_TABLE);
                } else {
                    lootTable = this.getWorld().getServer().getReloadableRegistries().getLootTable(BAD_FISHING_LOOT_TABLE);
                }
                List<ItemStack> list = lootTable.generateLoot(lootContextParameterSet);
//                Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this, list);

                for (ItemStack itemStack : list) {
                    ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), itemStack);
                    double d = playerEntity.getX() - this.getX();
                    double e = playerEntity.getY() - this.getY();
                    double f = playerEntity.getZ() - this.getZ();
                    itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                    this.getWorld().spawnEntity(itemEntity);
                    playerEntity.getWorld()
                            .spawnEntity(
                                    new ExperienceOrbEntity(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, this.random.nextInt(6) + 1)
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

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PULL_HOOKED_ENTITY
                && this.getWorld().isClient
                && this.hookedEntity instanceof PlayerEntity
                && ((PlayerEntity) this.hookedEntity).isMainPlayer()) {
            this.pullHookedEntity(this.hookedEntity);
        }

        super.handleStatus(status);
    }

    protected void pullHookedEntity(Entity entity) {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            Vec3d vec3d = new Vec3d(entity2.getX() - this.getX(), entity2.getY() - this.getY(), entity2.getZ() - this.getZ()).multiply(0.1);
            entity.setVelocity(entity.getVelocity().add(vec3d));
        }
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.setPlayerFishHook(null);
        super.remove(reason);
    }

    @Override
    public void onRemoved() {
        this.setPlayerFishHook(null);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.setPlayerFishHook(this);
    }

    private void setPlayerFishHook(@Nullable AbstractBobberEntity fishingBobber) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity != null) {
            ((PlayerEntityAccessor) playerEntity).meteorology$setFishingHook(fishingBobber);
        }
    }

    @Nullable
    public PlayerEntity getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
    }

    @Nullable
    public Entity getHookedEntity() {
        return this.hookedEntity;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity entity = this.getOwner();
        return new EntitySpawnS2CPacket(this, entityTrackerEntry, entity == null ? this.getId() : entity.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (this.getPlayerOwner() == null) {
            int i = packet.getEntityData();
            LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", this.getWorld().getEntityById(i), i);
            this.kill();
        }
    }

    public boolean getShowExclamationMark() {
        return this.showExclamationMark;
    }

    public void setShowExclamationMark(boolean showExclamationMark) {
        this.showExclamationMark = showExclamationMark;
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
