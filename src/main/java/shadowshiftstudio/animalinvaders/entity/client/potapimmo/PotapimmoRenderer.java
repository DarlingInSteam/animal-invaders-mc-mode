package shadowshiftstudio.animalinvaders.entity.client.potapimmo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;

public class PotapimmoRenderer extends MobRenderer<PotapimmoEntity, PotapimmoModel<PotapimmoEntity>> {
    public PotapimmoRenderer(EntityRendererProvider.Context p_174304_) {
        super(p_174304_, new PotapimmoModel<>(p_174304_.bakeLayer(PotapimmoModelLayers.POTAPIMMO_LAYER)), 1f);
    }

    @Override
    public ResourceLocation getTextureLocation(PotapimmoEntity potapimmoEntity) {
        return new ResourceLocation(AnimalInvaders.MOD_ID, "textures/entity/potapimmo.png");
    }

    @Override
    public void render(PotapimmoEntity p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_,
                       MultiBufferSource p_115459_, int p_115460_) {
        if (p_115455_.isBaby()) {
            p_115458_.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
    }
}
