package shadowshiftstudio.animalinvaders.entity.utils;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class EntityUtils {
    public static void applyHeadRotation(ModelPart head, float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

        head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }
}
