package net.pacmanmvc.meteorology.enchantment.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record ZoomEnchantmentEffect(EnchantmentLevelBasedValue amount) implements EnchantmentEntityEffect {
    public static final MapCodec<ZoomEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    EnchantmentLevelBasedValue.CODEC.fieldOf("amount").forGetter(ZoomEnchantmentEffect::amount)
            ).apply(instance, ZoomEnchantmentEffect::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        float maxVelocity = 1.5f;
        LivingEntity livingEntity = context.owner();
        if (!world.isClient) {
            if (livingEntity != null && livingEntity.isFallFlying()) {
                float pitchRadians = livingEntity.getPitch() * (float) (Math.PI / 180.0);
                Vec3d velocityVector = livingEntity.getVelocity();
                if (pitchRadians > 0.0F) {
                    Vec3d velocity = velocityVector.multiply(1.05, 1, 1.05);
                    Vec3d clampedVelocity = clampXZ(velocity, -maxVelocity, -maxVelocity, maxVelocity, maxVelocity);
                    livingEntity.setVelocity(clampedVelocity);
                    livingEntity.velocityModified = true;
                }
            }
        }
    }

    private static Vec3d clampXZ(Vec3d vec, double minX, double minZ, double maxX, double maxZ) {
        double x = Math.max(Math.min(vec.x, maxX), minX);
        double z = Math.max(Math.min(vec.z, maxZ), minZ);
        return new Vec3d(x, vec.y, z);
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
