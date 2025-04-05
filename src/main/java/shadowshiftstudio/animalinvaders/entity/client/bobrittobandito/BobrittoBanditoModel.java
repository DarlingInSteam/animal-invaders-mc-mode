package shadowshiftstudio.animalinvaders.entity.client.bobrittobandito;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import shadowshiftstudio.animalinvaders.entity.animations.bobrittobandito.BobrittoBanditoAnimationsDefinitions;
import shadowshiftstudio.animalinvaders.entity.animations.potapimmo.PotapimmoAnimationsDefinitions;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

public class BobrittoBanditoModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "bobrittobandito"), "main");
    private final ModelPart bobrittobandito;
    private final ModelPart body;
    private final ModelPart hand1;
    private final ModelPart hand2;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart mouth;
    private final ModelPart glasses;
    private final ModelPart gun;
    private final ModelPart magazine;

    public BobrittoBanditoModel(ModelPart root) {
        this.bobrittobandito = root.getChild("bobrittobandito");
        this.body = this.bobrittobandito.getChild("body");
        this.hand1 = this.bobrittobandito.getChild("hand1");
        this.hand2 = this.bobrittobandito.getChild("hand2");
        this.leg1 = this.bobrittobandito.getChild("leg1");
        this.leg2 = this.bobrittobandito.getChild("leg2");
        this.head = this.bobrittobandito.getChild("head");
        this.hat = this.head.getChild("hat");
        this.mouth = this.head.getChild("mouth");
        this.glasses = this.head.getChild("glasses");
        this.gun = this.bobrittobandito.getChild("gun");
        this.magazine = this.gun.getChild("magazine");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bobritto = partdefinition.addOrReplaceChild("bobrittobandito", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition body = bobritto.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -12.0F, -3.0F, 12.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.0886F, 0.1739F, -0.0154F));

        PartDefinition hand1 = bobritto.addOrReplaceChild("hand1", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.25F, -19.0F, 2.0F, -0.3961F, 0.134F, -0.0088F));

        PartDefinition arm_r1 = hand1.addOrReplaceChild("arm_r1", CubeListBuilder.create().texOffs(0, 57).addBox(-1.5F, 0.0F, -1.25F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, -0.6956F, 5.203F, 1.6424F, 1.0624F, -0.322F));

        PartDefinition hand_r1 = hand1.addOrReplaceChild("hand_r1", CubeListBuilder.create().texOffs(50, 48).addBox(-1.5F, 0.0F, -1.25F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.25F, 0.0F, 1.6771F, 0.6778F, -0.1063F));

        PartDefinition hand2 = bobritto.addOrReplaceChild("hand2", CubeListBuilder.create(), PartPose.offsetAndRotation(6.25F, -18.5F, 0.5F, -0.3052F, -0.0131F, -0.0416F));

        PartDefinition arm_r2 = hand2.addOrReplaceChild("arm_r2", CubeListBuilder.create().texOffs(13, 57).addBox(-1.0F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 0.25F, 0.25F, 1.3597F, 0.595F, -0.2843F));

        PartDefinition hand_r2 = hand2.addOrReplaceChild("hand_r2", CubeListBuilder.create().texOffs(54, 0).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, 0.3044F, 4.203F, 2.2923F, -0.5528F, -0.369F));

        PartDefinition leg1 = bobritto.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(33, 43).addBox(-1.5F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-1.5F, 11.0F, -2.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -13.0F, 0.0F));

        PartDefinition leg2 = bobritto.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(37, 0).addBox(-2.5F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(41, 16).addBox(-2.5F, 11.0F, -2.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -13.0F, 0.0F));

        PartDefinition head = bobritto.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 31).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -22.0F, 0.0F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(33, 31).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(58, 12).addBox(-4.5F, -1.5F, -4.9F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(21, 48).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.5F, 4.9F));

        PartDefinition glasses = head.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(26, 59).addBox(-5.0F, -1.0F, -1.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(41, 25).addBox(-5.0F, -1.0F, 5.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(41, 28).addBox(-3.0F, 0.0F, 5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 28).addBox(1.0F, 0.0F, 5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 58).addBox(4.0F, -1.0F, -1.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -1.0F));

        PartDefinition gun = bobritto.addOrReplaceChild("gun", CubeListBuilder.create().texOffs(3, 70).addBox(-2.25F, -1.0F, -0.5F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 70).addBox(8.75F, -2.0F, 0.5F, 1.0F, 0.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(14, 73).addBox(8.25F, -2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.25F, -21.25F, 7.25F, -3.081F, -1.3679F, 2.7347F));

        PartDefinition cube_r1 = gun.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(39, 86).addBox(-5.0F, 0.0F, -0.4F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

        PartDefinition cube_r2 = gun.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 85).addBox(-2.1F, 0.0F, -0.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -0.5F, 0.0F, 0.0F, 0.0F, -1.1345F));

        PartDefinition cube_r3 = gun.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(7, 76).addBox(-3.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.25F, 0.25F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition magazine = gun.addOrReplaceChild("magazine", CubeListBuilder.create().texOffs(5, 82).addBox(-0.5F, 0.0F, -0.4F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.25F, -0.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }



    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);
        this.animateWalk(BobrittoBanditoAnimationsDefinitions.Walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(((BobrittoBanditoEntity) entity).idleAnimationState, BobrittoBanditoAnimationsDefinitions.Idle, ageInTicks, 1f);
        this.animate(((BobrittoBanditoEntity) entity).attackAnimationState, BobrittoBanditoAnimationsDefinitions.Shooting, ageInTicks, 1f);
        this.animate(((BobrittoBanditoEntity) entity).runAnimationState, BobrittoBanditoAnimationsDefinitions.Run, ageInTicks, 1f);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch, float pAgeInTicks) {
        EntityUtils.applyHeadRotation(
                this.head,
                pNetHeadYaw,
                pHeadPitch
        );
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bobrittobandito.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return bobrittobandito;
    }
}
