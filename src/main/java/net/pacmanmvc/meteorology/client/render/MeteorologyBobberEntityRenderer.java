package net.pacmanmvc.meteorology.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.pacmanmvc.meteorology.Meteorology;
import net.pacmanmvc.meteorology.entity.projectile.AbstractBobberEntity;
import net.pacmanmvc.meteorology.util.ModTags;
import org.slf4j.Logger;


@Environment(EnvType.CLIENT)
public abstract class MeteorologyBobberEntityRenderer extends EntityRenderer<AbstractBobberEntity> {
    private static final Logger LOGGER = Meteorology.LOGGER;
    protected static Identifier TEXTURE = Identifier.ofVanilla("textures/entity/fishing_hook.png");
    private static final Identifier EXCLAMATION_MARK_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/world_list/error_highlighted.png");
    private static final RenderLayer EXCLAMATION_MARK_LAYER = RenderLayer.getEntityCutout(EXCLAMATION_MARK_TEXTURE);

    public MeteorologyBobberEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public void render(AbstractBobberEntity bobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        PlayerEntity playerEntity = bobberEntity.getPlayerOwner();
        if (playerEntity != null) {
            matrixStack.push();
            matrixStack.push();
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            matrixStack.multiply(this.dispatcher.getRotation());
            MatrixStack.Entry entry = matrixStack.peek();
            renderLayer(matrixStack, vertexConsumerProvider, i, entry, RenderLayer.getEntityCutout(getTexture(bobberEntity)));

            if (bobberEntity.getShowExclamationMark()) {
                matrixStack.push();
                MatrixStack.Entry entry3 = matrixStack.peek();
                matrixStack.scale(1.75f, 1.75f, 1.75f);
                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.translate(0.23, 0.75, 0);
                renderLayer(matrixStack, vertexConsumerProvider, i, entry3, EXCLAMATION_MARK_LAYER);
            }

            float h = playerEntity.getHandSwingProgress(g);
            float j = MathHelper.sin(MathHelper.sqrt(h) * (float) Math.PI);
            Vec3d vec3d = this.getHandPos(playerEntity, j, g);
            Vec3d vec3d2 = bobberEntity.getLerpedPos(g).add(0.0, 0.25, 0.0);
            float k = (float) (vec3d.x - vec3d2.x);
            float l = (float) (vec3d.y - vec3d2.y);
            float m = (float) (vec3d.z - vec3d2.z);
            VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLineStrip());
            MatrixStack.Entry entry2 = matrixStack.peek();

            for (int o = 0; o <= 16; o++) {
                renderFishingLine(k, l, m, vertexConsumer2, entry2, percentage(o, 16), percentage(o + 1, 16));
            }

            matrixStack.pop();
            super.render(bobberEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    private void renderLayer(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, MatrixStack.Entry entry3, RenderLayer testLayer) {
        VertexConsumer testVertexConsumer = vertexConsumerProvider.getBuffer(testLayer);
        vertex(testVertexConsumer, entry3, i, 0.0F, 0, 0, 1);
        vertex(testVertexConsumer, entry3, i, 1.0F, 0, 1, 1);
        vertex(testVertexConsumer, entry3, i, 1.0F, 1, 1, 0);
        vertex(testVertexConsumer, entry3, i, 0.0F, 1, 0, 0);
        matrixStack.pop();
    }

    private Vec3d getHandPos(PlayerEntity player, float f, float tickDelta) {
        int i = player.getMainArm() == Arm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandStack();
        if (!itemStack.isIn(ModTags.Items.FISHING_RODS)) {
            i = -i;
        }

        if (this.dispatcher.gameOptions.getPerspective().isFirstPerson() && player == MinecraftClient.getInstance().player) {
            double m = 960.0 / (double) this.dispatcher.gameOptions.getFov().getValue();
            Vec3d vec3d = this.dispatcher.camera.getProjection().getPosition((float) i * 0.525F, -0.1F).multiply(m).rotateY(f * 0.5F).rotateX(-f * 0.7F);
            return player.getCameraPosVec(tickDelta).add(vec3d);
        } else {
            float g = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw) * (float) (Math.PI / 180.0);
            double d = MathHelper.sin(g);
            double e = MathHelper.cos(g);
            float h = player.getScale();
            double j = (double) i * 0.35 * (double) h;
            double k = 0.8 * (double) h;
            float l = player.isInSneakingPose() ? -0.1875F : 0.0F;
            return player.getCameraPosVec(tickDelta).add(-e * j - d * k, (double) l - 0.45 * (double) h, -d * j + e * k);
        }
    }

    private static float percentage(int value, int max) {
        return (float) value / (float) max;
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry matrix, int light, float x, int y, int u, int v) {
        buffer.vertex(matrix, x - 0.5F, (float) y - 0.5F, 0.0F)
                .color(Colors.WHITE)
                .texture((float) u, (float) v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrix, 0.0F, 1.0F, 0.0F);
    }

    private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
        float f = x * segmentStart;
        float g = y * (segmentStart * segmentStart + segmentStart) * 0.5F + 0.25F;
        float h = z * segmentStart;
        float i = x * segmentEnd - f;
        float j = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5F + 0.25F - g;
        float k = z * segmentEnd - h;
        float l = MathHelper.sqrt(i * i + j * j + k * k);
        i /= l;
        j /= l;
        k /= l;
        buffer.vertex(matrices, f, g, h).color(Colors.BLACK).normal(matrices, i, j, k);
    }

    @Override
    public Identifier getTexture(AbstractBobberEntity entity) {
        return TEXTURE;
    }
}
