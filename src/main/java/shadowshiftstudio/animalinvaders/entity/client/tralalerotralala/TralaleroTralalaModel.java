package shadowshiftstudio.animalinvaders.entity.client.tralalerotralala;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.animations.potapimmo.PotapimmoAnimationsDefinitions;
import shadowshiftstudio.animalinvaders.entity.animations.tralalerotralala.TralaleroTralalaAnimationsDefinitions;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;
import shadowshiftstudio.animalinvaders.entity.custom.tralalerotralala.TralaleroTralalaEntity;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

public class TralaleroTralalaModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "tralalerotralala"), "main");
    private final ModelPart TralaleroTralala;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart plavnik;
    private final ModelPart nail;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public TralaleroTralalaModel(ModelPart root) {
        this.TralaleroTralala = root.getChild("tralalerotralala");
        this.body = this.TralaleroTralala.getChild("body");
        this.head = this.body.getChild("head");
        this.mouth = this.head.getChild("mouth");
        this.plavnik = this.body.getChild("plavnik");
        this.nail = this.body.getChild("nail");
        this.leg1 = this.TralaleroTralala.getChild("leg1");
        this.leg2 = this.TralaleroTralala.getChild("leg2");
        this.leg3 = this.TralaleroTralala.getChild("leg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition TralaleroTralala = partdefinition.addOrReplaceChild("tralalerotralala", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = TralaleroTralala.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -16.0F, -10.0F, 8.0F, 8.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(34, 25).addBox(-3.0F, -15.0F, 7.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 25).addBox(-4.0F, -3.0F, -12.0F, 8.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-3.0F, 1.0F, -6.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 45).addBox(-3.0F, 1.0F, -9.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, -7.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(18, 46).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -8.0F));

        PartDefinition plavnik = body.addOrReplaceChild("plavnik", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, -2.0F));

        PartDefinition cube_r1 = plavnik.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(34, 35).addBox(-0.5F, -4.0F, -1.0F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.1642F, 1.4142F, 0.7854F, 0.0F, 0.0F));

        PartDefinition nail = body.addOrReplaceChild("nail", CubeListBuilder.create().texOffs(50, 10).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 10.0F));

        PartDefinition cube_r2 = nail.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(18, 38).addBox(-0.5F, -2.0F, 0.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.8358F, 3.1642F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r3 = nail.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 46).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.1642F, 4.4142F, 0.7854F, 0.0F, 0.0F));

        PartDefinition leg1 = TralaleroTralala.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(46, 46).addBox(-2.0F, 9.0F, -3.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(50, 35).addBox(-2.0F, 8.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -10.0F, -4.25F));

        PartDefinition cube_r4 = leg1.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(50, 41).addBox(0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7775F, 4.722F, -1.0F, 0.0F, 0.0F, 0.0436F));

        PartDefinition cube_r5 = leg1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(50, 15).addBox(-0.5F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition leg2 = TralaleroTralala.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(50, 22).addBox(0.0F, 8.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(50, 0).addBox(0.0F, 9.0F, -3.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -10.0F, -4.25F));

        PartDefinition cube_r6 = leg2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(29, 37).addBox(-1.5F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7775F, 4.722F, -1.0F, 0.0F, 0.0F, -0.0436F));

        PartDefinition cube_r7 = leg2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(18, 50).addBox(-1.5F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition leg3 = TralaleroTralala.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(50, 38).addBox(-1.0F, 7.278F, -1.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(50, 5).addBox(-1.0F, 8.278F, -3.25F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).addBox(-0.5F, 4.028F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.278F, 7.5F));

        PartDefinition cube_r8 = leg3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(26, 50).mirror().addBox(-0.5F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(26, 50).addBox(0.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -0.222F, 0.5F, -0.0873F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);

        this.animateWalk(TralaleroTralalaAnimationsDefinitions.walk, limbSwing, limbSwingAmount, 2f, 2.5f);

        this.animate(((TralaleroTralalaEntity) entity).idleAnimationState, TralaleroTralalaAnimationsDefinitions.idle, ageInTicks, 1f);
        this.animate(((TralaleroTralalaEntity) entity).attackAnimationState, TralaleroTralalaAnimationsDefinitions.attack, ageInTicks, 1f);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch, float pAgeInTicks) {
        EntityUtils.applyHeadRotation(
                this.head,
                pNetHeadYaw,
                pHeadPitch
        );
    }

    @Override
    public ModelPart root() {
        return this.TralaleroTralala;
    }
}
