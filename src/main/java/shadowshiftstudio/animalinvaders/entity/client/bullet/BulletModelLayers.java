package shadowshiftstudio.animalinvaders.entity.client.bullet;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import shadowshiftstudio.animalinvaders.AnimalInvaders;

public class BulletModelLayers {
    public static final ModelLayerLocation BULLET_LAYER = new ModelLayerLocation(
            new ResourceLocation(AnimalInvaders.MOD_ID, "bullet"), "main");
}