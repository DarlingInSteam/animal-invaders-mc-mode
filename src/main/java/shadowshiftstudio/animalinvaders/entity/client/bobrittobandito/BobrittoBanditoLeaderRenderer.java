package shadowshiftstudio.animalinvaders.entity.client.bobrittobandito;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoLeaderEntity;

public class BobrittoBanditoLeaderRenderer extends MobRenderer<BobrittoBanditoLeaderEntity, BobrittoBanditoLeaderModel<BobrittoBanditoLeaderEntity>> {
    
    // Использую отдельную текстуру для лидера
    private static final ResourceLocation TEXTURE = 
            new ResourceLocation(AnimalInvaders.MOD_ID, "textures/entity/bobritto_leader.png");
    
    public BobrittoBanditoLeaderRenderer(EntityRendererProvider.Context context) {
        // Используем специальную модель для лидера
        super(context, new BobrittoBanditoLeaderModel<>(context.bakeLayer(BobrittoBanditoLeaderModelLayers.BOBRITTO_LEADER_LAYER)), 1.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(BobrittoBanditoLeaderEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BobrittoBanditoLeaderEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Надпись "Лидер" над головой всегда отображается у лидера
        renderNameTag(entity, net.minecraft.network.chat.Component.literal("§cЛидер"), poseStack, buffer, packedLight);
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}