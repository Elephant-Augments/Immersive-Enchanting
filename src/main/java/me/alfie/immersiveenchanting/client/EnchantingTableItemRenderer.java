package me.alfie.immersiveenchanting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.IEnchantingTableInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Block-entity renderer that draws the tool to enchant floating above the
 * enchanting table, tied to the book opening animation so the item fades in
 * and grows as the book opens, then settles into a hover-and-spin idle.
 *
 * <p>Extends {@link EnchantTableRenderer} so the vanilla book animation is
 * preserved unchanged; the floating item is drawn on top after the super
 * call.</p>
 */
public class EnchantingTableItemRenderer extends EnchantTableRenderer {

    public EnchantingTableItemRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EnchantingTableBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        // Vanilla book first.
        super.render(blockEntity, partialTicks, poseStack, bufferIn, combinedLightIn, combinedOverlayIn);

        if (!(blockEntity instanceof IEnchantingTableInventory inventory)) return;

        ItemStack tool = inventory.immersive$getItem(0);
        if (tool.isEmpty()) return;

        // Skip drawing while the book is shut and not animating, so the item
        // doesn't pop into a closed table.
        if (blockEntity.open <= 0.0F && blockEntity.oOpen <= 0.0F) return;

        renderFloatingTool(blockEntity, tool, partialTicks, poseStack, bufferIn, combinedLightIn);
    }

    private void renderFloatingTool(EnchantingTableBlockEntity blockEntity, ItemStack stack,
                                    float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                                    int light) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(stack, blockEntity.getLevel(), null, 0);

        // Smoothly interpolated openness in [0, 1] coming from the vanilla
        // book animation — drives the fade-in / grow-in of the item.
        float openness = Mth.lerp(partialTicks, blockEntity.oOpen, blockEntity.open);

        // Bob: simple sine of the BE clock so the item floats up and down.
        float clock = blockEntity.time + partialTicks;
        float bob = Mth.sin(clock / 20.0F) * 0.05F + 0.1F;

        // Vertical offset that accounts for both the bob and an extra lift
        // proportional to how open the book is, to keep the item seated on
        // top of the table even with items that have weird ground transforms.
        float modelYScale = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        float yLift = bob + 0.25F * modelYScale * openness - 0.15F * (1.0F - openness);

        poseStack.pushPose();

        // Centre on the block top, then lift higher so the item floats above
        poseStack.translate(0.5F, 1.1F, 0.5F);
        poseStack.translate(0.0F, yLift, 0.0F);

        // Scale grows with openness so the item appears as the book opens.
        float scale = openness * 0.8F + 0.2F;
        poseStack.scale(scale, scale, scale);

        // Spin around vertical axis.
        poseStack.mulPose(Axis.YP.rotation(clock / 50.0F));

        mc.getItemRenderer().render(
                stack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                buffer,
                light,
                OverlayTexture.NO_OVERLAY,
                model
        );

        poseStack.popPose();
    }
}