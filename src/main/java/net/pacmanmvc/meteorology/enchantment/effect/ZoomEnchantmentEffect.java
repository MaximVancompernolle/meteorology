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
        LivingEntity livingEntity = context.owner();
        if (!world.isClient) {
            if (livingEntity != null && livingEntity.isFallFlying()) {
                Vec3d vecVelocity = livingEntity.getVelocity();
                float amt = amount.getValue(level) + 0.2f;
                livingEntity
                        .setVelocity(clamp(vecVelocity.multiply(amt, 1, amt), -1, -1, -1, 1, 1, 1));
                livingEntity.velocityModified = true;
            }
        }
    }

    private static Vec3d clamp(Vec3d vec, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double x = Math.max(Math.min(vec.x, maxX), minX);
        double y = Math.max(Math.min(vec.y, maxY), minY);
        double z = Math.max(Math.min(vec.z, maxZ), minZ);
        return new Vec3d(x, y, z);
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
