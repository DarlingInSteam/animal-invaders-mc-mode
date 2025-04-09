package shadowshiftstudio.animalinvaders.entity.client.lirililarila;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.lirililarila.LiriliLarilaEntity;

public class LiriliLarilaRenderer extends MobRenderer<LiriliLarilaEntity, LiriliLarilaModel<LiriliLarilaEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnimalInvaders.MOD_ID, "textures/entity/lirililarila.png");
    
    public LiriliLarilaRenderer(EntityRendererProvider.Context context) {
        super(context, new LiriliLarilaModel<>(context.bakeLayer(LiriliLarilaModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(LiriliLarilaEntity entity) {
        return TEXTURE;
    }
    
    @Override
    public void render(LiriliLarilaEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}