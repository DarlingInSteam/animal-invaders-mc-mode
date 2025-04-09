package shadowshiftstudio.animalinvaders.entity.client.tralalerotralala;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.tralalerotralala.TralaleroTralalaEntity;

public class TralaleroTralalaRenderer extends MobRenderer<TralaleroTralalaEntity, TralaleroTralalaModel<TralaleroTralalaEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnimalInvaders.MOD_ID, "textures/entity/tralalerotralala.png");

    public TralaleroTralalaRenderer(EntityRendererProvider.Context context) {
        super(context, new TralaleroTralalaModel<>(context.bakeLayer(TralaleroTralalaModelLayers.TRALALEROTRALALA_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TralaleroTralalaEntity entity) {
        return TEXTURE;
    }
}