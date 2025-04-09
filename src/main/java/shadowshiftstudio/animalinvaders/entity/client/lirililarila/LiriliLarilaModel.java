package shadowshiftstudio.animalinvaders.entity.client.lirililarila;// Made with Blockbench 4.12.4
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
import net.minecraft.world.entity.Entity;
import shadowshiftstudio.animalinvaders.entity.animations.lirililarila.LiriliLarilaAnimationsDefinitions;
import shadowshiftstudio.animalinvaders.entity.custom.lirililarila.LiriliLarilaEntity;

public class LiriliLarilaModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "lirililrila"), "main");
	private final ModelPart LiriliLarila;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart ear1;
	private final ModelPart ear2;
	private final ModelPart nose;

	public LiriliLarilaModel(ModelPart root) {
		this.LiriliLarila = root.getChild("LiriliLarila");
		this.leg1 = this.LiriliLarila.getChild("leg1");
		this.leg2 = this.LiriliLarila.getChild("leg2");
		this.body = this.LiriliLarila.getChild("body");
		this.head = this.body.getChild("head");
		this.ear1 = this.head.getChild("ear1");
		this.ear2 = this.head.getChild("ear2");
		this.nose = this.head.getChild("nose");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition LiriliLarila = partdefinition.addOrReplaceChild("LiriliLarila", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition leg1 = LiriliLarila.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 66).addBox(-4.5F, -0.25F, -3.0F, 9.0F, 19.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(42, 46).addBox(-5.5F, 18.0F, -9.0F, 11.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(68, 24).addBox(-5.5F, 15.0F, -2.0F, 11.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(28, 80).addBox(-4.5F, 15.75F, -8.0F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.0F, -6.0F));

		PartDefinition cube_r1 = leg1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 72).addBox(-4.0F, -2.0F, -3.0F, 9.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 16.9054F, -4.5846F, 0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r2 = leg1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(84, 80).addBox(-5.5F, -3.0F, -1.0F, 11.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.5F, -3.25F, 0.7854F, 0.0F, 0.0F));

		PartDefinition leg2 = LiriliLarila.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(68, 0).addBox(-4.5F, -0.5F, -3.0F, 9.0F, 19.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(42, 59).addBox(-5.5F, 18.0F, -9.0F, 11.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(68, 32).addBox(-5.5F, 15.0F, -2.0F, 11.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(56, 80).addBox(-4.5F, 15.75F, -8.0F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.0F, 7.0F));

		PartDefinition cube_r3 = leg2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(58, 72).addBox(-4.0F, -2.0F, -3.0F, 9.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 16.9054F, -4.5846F, 0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r4 = leg2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(28, 87).addBox(-5.5F, -3.0F, -1.0F, 11.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.5F, -3.25F, 0.7854F, 0.0F, 0.0F));

		PartDefinition body = LiriliLarila.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -40.0F, -10.0F, 14.0F, 23.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 46).addBox(-6.0F, -10.0F, -9.0F, 12.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -29.0F, -10.0F));

		PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(88, 55).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 2.0F, -8.0F, -0.5087F, -0.1298F, -0.228F));

		PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(88, 48).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 2.0F, -8.0F, -0.5087F, 0.1298F, 0.228F));

		PartDefinition ear1 = head.addOrReplaceChild("ear1", CubeListBuilder.create(), PartPose.offset(-6.0F, -5.0F, -1.0F));

		PartDefinition cube_r7 = ear1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(54, 87).addBox(-7.0F, -6.0F, 0.0F, 7.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition ear2 = head.addOrReplaceChild("ear2", CubeListBuilder.create(), PartPose.offset(6.0F, -5.0F, -1.0F));

		PartDefinition cube_r8 = ear2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(70, 87).addBox(0.0F, -6.0F, 0.0F, 7.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(86, 87).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -9.0F));

		PartDefinition cube_r9 = nose.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(68, 40).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.8735F, -6.5084F, -2.2689F, 0.0F, 0.0F));

		PartDefinition cube_r10 = nose.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(28, 66).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.37F, -4.0124F, -1.2654F, 0.0F, 0.0F));

		PartDefinition cube_r11 = nose.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(88, 40).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.6067F, -1.5551F, -0.3054F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		LiriliLarilaEntity lirililarila = (LiriliLarilaEntity) entity;

		// Apply head rotation only if not hiding
		if (!lirililarila.isHiding()) {
			this.applyHeadRotation(netHeadYaw, headPitch);
		}

		// Apply walk animation if moving
		this.animateWalk(LiriliLarilaAnimationsDefinitions.walk, limbSwing, limbSwingAmount, 2f, 2.5f);

		// Apply the appropriate animation based on state
		if (lirililarila.isHiding()) {
			this.animate(lirililarila.hideInAnimationState, LiriliLarilaAnimationsDefinitions.hide_in, ageInTicks, 1f);
		} else {
			this.animate(lirililarila.hideOutAnimationState, LiriliLarilaAnimationsDefinitions.hide_out, ageInTicks, 1f);
			this.animate(lirililarila.idleAnimationState, LiriliLarilaAnimationsDefinitions.idle, ageInTicks, 1f);
		}
	}

	private void applyHeadRotation(float netHeadYaw, float headPitch) {
		netHeadYaw = netHeadYaw * ((float)Math.PI / 180F);
		headPitch = headPitch * ((float)Math.PI / 180F);

		this.head.xRot = headPitch;
		this.head.yRot = netHeadYaw;
	}

	@Override
	public ModelPart root() {
		return this.LiriliLarila;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		LiriliLarila.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}