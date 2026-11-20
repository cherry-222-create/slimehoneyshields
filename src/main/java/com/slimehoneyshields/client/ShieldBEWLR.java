package com.slimehoneyshields.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.slimehoneyshields.SlimeHoneyShields;
import com.slimehoneyshields.util.ShieldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Renders the shields as the vanilla shield model/texture plus a 2-pixel-thick slime or honey
 * layer glued onto the front face.
 *
 * <p>Vanilla's own {@code BlockEntityWithoutLevelRenderer} only handles {@code Items.SHIELD}, so a
 * custom shield item needs its own renderer. The base plate and handle use the vanilla no-pattern
 * shield texture; a small extra cuboid (12x22x2) sits on the shield face with the slime/honey block
 * texture.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ShieldBEWLR extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/shield_base_nopattern.png");
    private static final ResourceLocation SLIME_OVERLAY =
            new ResourceLocation(SlimeHoneyShields.MOD_ID, "textures/entity/slime_overlay.png");
    private static final ResourceLocation HONEY_OVERLAY =
            new ResourceLocation(SlimeHoneyShields.MOD_ID, "textures/entity/honey_overlay.png");

    private static ShieldBEWLR instance;

    private final ShieldModel model;
    private final ModelPart overlay;

    private ShieldBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.model = new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHIELD));

        // The vanilla plate is 12x22x1 at z=-2..-1. This cuboid adds exactly two pixels on the
        // front of it (z=-4..-2), so the result reads as a wooden shield with a thick slime/honey
        // face rather than a recoloured shield.
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("overlay",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-6.0F, -11.0F, -4.0F, 12.0F, 22.0F, 2.0F),
                PartPose.ZERO);
        this.overlay = LayerDefinition.create(mesh, 64, 64).bakeRoot().getChild("overlay");
    }

    public static ShieldBEWLR get() {
        if (instance == null) {
            instance = new ShieldBEWLR();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!ShieldHelper.isShield(stack)) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);

        // Vanilla wooden shield: handle then plate, exactly like the vanilla shield renderer.
        VertexConsumer baseConsumer = ItemRenderer.getFoilBufferDirect(buffer,
                this.model.renderType(BASE_TEXTURE), true, stack.hasFoil());
        this.model.handle().render(poseStack, baseConsumer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);
        this.model.plate().render(poseStack, baseConsumer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);

        // The 2-pixel slime/honey face layer.
        ResourceLocation overlayTexture = ShieldHelper.isSlime(stack) ? SLIME_OVERLAY : HONEY_OVERLAY;
        VertexConsumer overlayConsumer = ItemRenderer.getFoilBufferDirect(buffer,
                RenderType.entityTranslucent(overlayTexture), true, stack.hasFoil());
        this.overlay.render(poseStack, overlayConsumer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
