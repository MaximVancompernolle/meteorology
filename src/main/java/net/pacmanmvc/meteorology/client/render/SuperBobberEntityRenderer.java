package net.pacmanmvc.meteorology.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.entity.projectile.AbstractBobberEntity;

public class SuperBobberEntityRenderer extends MeteorologyBobberEntityRenderer {
    private static final Identifier TEXTURE = Identifier.of(Meteorology.MOD_ID, "textures/entity/super_bobber.png");
    public SuperBobberEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(AbstractBobberEntity entity) {
        return TEXTURE;
    }
}
