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
            if (livingEntity != null && livingEntity.isFallFlying() && livingEntity.getPitch() > 0.0) {
                float modifier = this.amount.getValue(level) * 0.01F;

                Vec3d velocity = livingEntity.getVelocity();
                Vec3d rotation = livingEntity.getRotationVector();

                livingEntity.setVelocity(velocity.add(
                        (rotation.x * 0.5 + (rotation.x * 1.5 - velocity.x) * 0.5) * modifier,
                        0,
                        (rotation.z * 0.5 + (rotation.z * 1.5 - velocity.z) * 0.5) * modifier
                ));
                livingEntity.velocityModified = true;
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
