package net.pacmanmvc.meteorology;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.entity.ModEntities;
import net.pacmanmvc.meteorology.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeteorologyClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Meteorology.MOD_ID);

    private static float getRodTexture(ItemStack itemStack, ClientWorld clientWorld, LivingEntity livingEntity, int seed) {
        if (livingEntity == null) {
            return 0.0F;
        } else {
            boolean bl = livingEntity.getMainHandStack() == itemStack;
            boolean bl2 = livingEntity.getOffHandStack() == itemStack;
            if (livingEntity.getMainHandStack().getItem() instanceof FishingRodItem) {
                bl2 = false;
            }

            return (bl || bl2) && livingEntity instanceof PlayerEntity && ((PlayerEntity) livingEntity).fishHook != null ? 1.0F : 0.0F;
        }
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Meteorology Client");
        ModelPredicateProviderRegistry.register(ModItems.GOOD_ROD, Identifier.ofVanilla("cast"), MeteorologyClient::getRodTexture);
        ModelPredicateProviderRegistry.register(ModItems.SUPER_ROD, Identifier.ofVanilla("cast"), MeteorologyClient::getRodTexture);
        EntityRendererRegistry.register(ModEntities.GOOD_BOBBER, FishingBobberEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SUPER_BOBBER, FishingBobberEntityRenderer::new);

    }
}
