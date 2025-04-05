package shadowshiftstudio.animalinvaders.entity.client.potapimmo;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


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
import shadowshiftstudio.animalinvaders.entity.animations.potapimmo.PotapimmoAnimationsDefinitions;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

public class PotapimmoModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "patapimmo"), "main");
	private final ModelPart patapimmo;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart nose;
	private final ModelPart eyebrow;
	private final ModelPart hand1;
	private final ModelPart hand2;
	private final ModelPart leg1;
	private final ModelPart underleg1;
	private final ModelPart leg2;
	private final ModelPart underleg2;

	public PotapimmoModel(ModelPart root) {
		this.patapimmo = root.getChild("patapimmo");
		this.body = this.patapimmo.getChild("body");
		this.head = this.body.getChild("head");
		this.nose = this.head.getChild("nose");
		this.eyebrow = this.head.getChild("eyebrow");
		this.hand1 = this.body.getChild("hand1");
		this.hand2 = this.body.getChild("hand2");
		this.leg1 = this.patapimmo.getChild("leg1");
		this.underleg1 = this.leg1.getChild("underleg1");
		this.leg2 = this.patapimmo.getChild("leg2");
		this.underleg2 = this.leg2.getChild("underleg2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition patapimmo = partdefinition.addOrReplaceChild("patapimmo", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = patapimmo.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 27).addBox(-8.0F, -13.0F, -4.0F, 16.0F, 13.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(9.0F, -11.0F, -4.25F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 96).addBox(8.0F, -13.0F, -4.5F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(16, 96).addBox(-9.0F, -13.0F, -4.5F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(80, 115).addBox(-10.0F, -11.0F, -4.25F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(84, 50).addBox(-8.0F, -13.0F, 2.75F, 16.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 59).addBox(-6.0F, -6.0F, 3.0F, 12.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 110).addBox(-4.0F, 0.0F, 3.25F, 8.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 63).addBox(-8.0F, -4.0F, -4.25F, 16.0F, 19.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -26.0F, 0.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(116, 16).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, 2.0F, 1.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(72, 102).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 2.0F, 1.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -3.0F, -11.0F, 10.0F, 17.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(46, 36).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(32, 66).addBox(-5.0F, -6.0F, -11.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 82).addBox(-6.0F, -3.0F, -1.25F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(102, 82).addBox(-4.0F, 0.25F, 0.75F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, -4.0F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(32, 77).addBox(0.0F, 0.0F, -6.0F, 0.0F, 11.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -3.0F, -6.0F, 0.0F, 0.0F, -0.0436F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(72, 66).addBox(0.0F, 0.0F, -6.0F, 0.0F, 11.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -3.0F, -6.0F, 0.0F, 0.0F, 0.0436F));

		PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create(), PartPose.offset(0.5F, 1.0F, -10.0F));

		PartDefinition cube_r5 = nose.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(16, 111).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition eyebrow = head.addOrReplaceChild("eyebrow", CubeListBuilder.create().texOffs(40, 25).addBox(-4.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 47).addBox(-2.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 63).addBox(-5.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 100).addBox(1.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 63).addBox(2.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 108).addBox(4.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -11.0F));

		PartDefinition hand1 = body.addOrReplaceChild("hand1", CubeListBuilder.create().texOffs(88, 13).addBox(0.0F, 13.0F, -1.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 99).addBox(0.0F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(40, 18).addBox(2.0F, 13.0F, -1.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 89).addBox(1.75F, 13.0F, 0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 116).addBox(2.25F, 12.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -9.0F, -2.5F));

		PartDefinition hand2 = body.addOrReplaceChild("hand2", CubeListBuilder.create().texOffs(92, 99).addBox(-3.0F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(28, 82).addBox(-3.0F, 13.0F, -1.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(96, 82).addBox(-2.75F, 13.0F, 0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(116, 31).addBox(-3.25F, 12.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 111).addBox(-1.0F, 13.0F, -1.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, -9.0F, -2.5F));

		PartDefinition leg1 = patapimmo.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(32, 108).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(32, 100).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -26.0F, 0.0F));

		PartDefinition underleg1 = leg1.addOrReplaceChild("underleg1", CubeListBuilder.create().texOffs(40, 0).addBox(-4.0F, 15.0F, -11.0F, 9.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(0, 47).addBox(-3.5F, 13.0F, -10.0F, 8.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(80, 89).addBox(-4.0F, 14.0F, -8.0F, 2.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(110, 59).addBox(-2.0F, 15.5F, -14.0F, 3.0F, 2.5F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 115).addBox(0.0F, 15.75F, -13.0F, 3.0F, 2.25F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(116, 23).addBox(2.0F, 16.0F, -12.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 110).addBox(-4.0F, 15.0F, -15.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(94, 43).addBox(-2.5F, 6.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(88, 0).addBox(-3.0F, 8.0F, -3.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(94, 13).addBox(3.0F, 14.0F, -8.0F, 2.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(56, 77).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(96, 66).addBox(-3.0F, 12.0F, -8.5F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition leg2 = patapimmo.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(104, 109).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(52, 102).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -26.0F, 0.0F));

		PartDefinition underleg2 = leg2.addOrReplaceChild("underleg2", CubeListBuilder.create().texOffs(104, 91).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(104, 102).addBox(-2.5F, 6.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(56, 89).addBox(-3.0F, 8.0F, -3.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(96, 74).addBox(-3.0F, 12.0F, -8.5F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(94, 23).addBox(-5.0F, 14.0F, -8.0F, 2.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(94, 33).addBox(2.0F, 14.0F, -8.0F, 2.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(116, 27).addBox(-5.0F, 16.0F, -12.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 18).addBox(-5.0F, 15.0F, -11.0F, 9.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(114, 43).addBox(-1.0F, 15.5F, -14.0F, 3.0F, 2.5F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(116, 11).addBox(-3.0F, 15.75F, -13.0F, 3.0F, 2.25F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(42, 50).addBox(-4.5F, 13.0F, -10.0F, 8.0F, 3.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(0, 111).addBox(1.0F, 15.0F, -15.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);

		this.animateWalk(PotapimmoAnimationsDefinitions.walk, limbSwing, limbSwingAmount, 2f, 2.5f);

		this.animate(((PotapimmoEntity) entity).idleAnimationState, PotapimmoAnimationsDefinitions.idle, ageInTicks, 1f);
		this.animate(((PotapimmoEntity) entity).attackAnimationState, PotapimmoAnimationsDefinitions.attack, ageInTicks, 1f);
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
		patapimmo.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return patapimmo;
	}
}