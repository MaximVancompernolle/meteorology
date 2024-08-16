package net.pacmanmvc.meteorology.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class ModFoodComponents {
    public static final FoodComponent GLISTERING_MELON_SLICE = new FoodComponent.Builder()
            .nutrition(2)
            .saturationModifier(0.3F)
            .statusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 0), 0.5F)
            .alwaysEdible()
            .build();

    public static final FoodComponent SUGAR = new FoodComponent.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1), 1.0F)
            .alwaysEdible()
            .build();
}
