package shadowshiftstudio.animalinvaders.entity.client.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.bullet.BulletEntity;

public class BulletRenderer extends EntityRenderer<BulletEntity> {
    private static final ResourceLocation BULLET_TEXTURE = new ResourceLocation(AnimalInvaders.MOD_ID, "textures/entity/bullet.png");
    private static final float BULLET_WIDTH = 0.15F;
    private static final float BULLET_HEIGHT = 0.15F;

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BulletEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, 
                      MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        
        // Поворот пули в направлении движения
        matrixStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        matrixStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        
        // Рендер пули как плоского квадрата, повернутого в соответствии с вектором движения
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        
        // Рисуем пулю как билборд (всегда направлен на камеру)
        vertex(matrix4f, matrix3f, vertexConsumer, -BULLET_WIDTH / 2, 0, 0, 0, 0, 1, 0, 1, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, BULLET_WIDTH / 2, 0, 0, 1, 0, 1, 0, 1, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, BULLET_WIDTH / 2, BULLET_HEIGHT, 0, 1, 1, 1, 0, 1, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, -BULLET_WIDTH / 2, BULLET_HEIGHT, 0, 0, 1, 1, 0, 1, packedLight);
        
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    private static void vertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, 
                              float x, float y, float z, float u, float v, 
                              int normalX, int normalY, int normalZ, int light) {
        vertexConsumer.vertex(matrix4f, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(matrix3f, normalX, normalZ, normalY)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BulletEntity entity) {
        return BULLET_TEXTURE;
    }
}