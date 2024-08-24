package net.pacmanmvc.meteorology.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.entity.projectile.GoodBobberEntity;
import net.pacmanmvc.meteorology.entity.projectile.SuperBobberEntity;

public class ModEntities {
//    public static EntityType<HarpoonEntity> HARPOON = Registry.register(
//            Registries.ENTITY_TYPE,
//            Identifier.of(Meteorology.MOD_ID, "harpoon"),
//            EntityType.Builder.<HarpoonEntity>create(HarpoonEntity::new, SpawnGroup.MISC)
//                    .dimensions(0.6F, 0.6F)
//                    .eyeHeight(0.15F)
//                    .maxTrackingRange(4)
//                    .trackingTickInterval(20)
//                    .build()
//    );

    public static EntityType<GoodBobberEntity> GOOD_BOBBER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Meteorology.MOD_ID, "good_bobber"),
            EntityType.Builder.<GoodBobberEntity>create(GoodBobberEntity::new, SpawnGroup.MISC)
                    .disableSaving()
                    .disableSummon()
                    .dimensions(0.3F, 0.3F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(5)
                    .build()
    );

    public static EntityType<SuperBobberEntity> SUPER_BOBBER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Meteorology.MOD_ID, "super_bobber"),
            EntityType.Builder.<SuperBobberEntity>create(SuperBobberEntity::new, SpawnGroup.MISC)
                    .disableSaving()
                    .disableSummon()
                    .dimensions(0.35F, 0.35F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(5)
                    .build()
    );
    public static void registerModEntities() {
        Meteorology.LOGGER.info("Registering Mod Entities for " + Meteorology.MOD_ID);
    }
}
