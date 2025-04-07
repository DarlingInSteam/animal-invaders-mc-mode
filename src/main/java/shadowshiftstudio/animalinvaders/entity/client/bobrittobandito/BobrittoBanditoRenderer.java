package shadowshiftstudio.animalinvaders.entity.client.bobrittobandito;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.client.potapimmo.PotapimmoModel;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;

public class BobrittoBanditoRenderer extends MobRenderer<BobrittoBanditoEntity, BobrittoBanditoModel<BobrittoBanditoEntity>> {
    public BobrittoBanditoRenderer(EntityRendererProvider.Context p_174304_) {
        super(p_174304_, new BobrittoBanditoModel<>(p_174304_.bakeLayer(BobrittoBanditoModelLayers.BOBRITTO_LAYER)), 1f);
    }

    @Override
    public ResourceLocation getTextureLocation(BobrittoBanditoEntity bobrittoBanditoEntity) {
        return new ResourceLocation(AnimalInvaders.MOD_ID, "textures/entity/bobritto.png");
    }

    @Override
    public void render(BobrittoBanditoEntity p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_,
                       MultiBufferSource p_115459_, int p_115460_) {
        if (p_115455_.isBaby()) {
            p_115458_.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
        
        // Добавляем надпись "Лидер" над головой лидера патрульной группы
        if (p_115455_.isPatrolLeader()) {
            renderNameTag(p_115455_, net.minecraft.network.chat.Component.literal("§cЛидер"), p_115458_, p_115459_, p_115460_);
        }
    }
}
