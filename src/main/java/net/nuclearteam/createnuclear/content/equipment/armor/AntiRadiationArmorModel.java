package net.nuclearteam.createnuclear.content.equipment.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * The suit model. 26.2 models are posed from a render state, and draws are deferred: the model is
 * posed when the submission is drawn, not when it is made. So the slot can no longer be a field
 * set just before rendering (upstream's {@code currentSlot}); instead there is one instance per
 * slot, each showing only its own parts ({@link AntiRadiationArmorRenderer}).
 */
public class AntiRadiationArmorModel extends HumanoidModel<HumanoidRenderState> {
    private final ModelPart left_boot;
    private final ModelPart right_boot;
    private final EquipmentSlot slot;

    public AntiRadiationArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root, RenderTypes::armorCutoutNoCull);
        this.left_boot = root.getChild("left_boot");
        this.right_boot = root.getChild("right_boot");
        this.slot = slot;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(30, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
                .texOffs(30, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        // 26.2's HumanoidModel looks the hat up under the head; keep it empty.
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 19).addBox(-3.7F, 0.0F, -2.8F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.4F, -1.0F, -3.1F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-4.0F, 0.0F, -2.7F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-4.5F, -1.0F, -3.0F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(45, 32).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
                .texOffs(29, 32).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(45, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
                .texOffs(29, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(29, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(45, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(29, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(45, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(61, 39).addBox(-2.0F, 8.3F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(61, 39).mirror().addBox(-2.0F, 8.3F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 96, 96);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        // Make the boots follow the (already animated) legs.
        this.right_boot.loadPose(this.rightLeg.storePose());
        this.left_boot.loadPose(this.leftLeg.storePose());

        // Leggings show the legs, boots only the boot parts: vanilla's leg parts serve both slots.
        this.head.visible = this.slot == EquipmentSlot.HEAD;
        this.hat.visible = false;
        this.body.visible = this.slot == EquipmentSlot.CHEST;
        this.rightArm.visible = this.slot == EquipmentSlot.CHEST;
        this.leftArm.visible = this.slot == EquipmentSlot.CHEST;
        this.rightLeg.visible = this.slot == EquipmentSlot.LEGS;
        this.leftLeg.visible = this.slot == EquipmentSlot.LEGS;
        this.right_boot.visible = this.slot == EquipmentSlot.FEET;
        this.left_boot.visible = this.slot == EquipmentSlot.FEET;
    }
}
