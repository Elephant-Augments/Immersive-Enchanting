package me.alfie.immersiveenchanting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.IEnchantingTableInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

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

    private final ItemStackRenderState itemState = new ItemStackRenderState();
    private float clock;
    private float openness;

    public EnchantingTableItemRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(EnchantTableRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // Vanilla book first.
        super.submit(state, poseStack, submitNodeCollector, camera);

        float bob = Mth.sin(clock / 20.0F) * 0.05F + 0.1F;
        float yLift = bob + 0.25F * openness - 0.15F * (1.0F - openness);

        poseStack.pushPose();

        poseStack.translate(0.5F, 1.1F, 0.5F);
        poseStack.translate(0.0F, yLift, 0.0F);

        //Scale grows with openness so the item appears as the book opens.
        float scale = openness * 0.8F + 0.2F;
        poseStack.scale(scale, scale, scale);

        //Spin around vertical axis.
        poseStack.mulPose(Axis.YP.rotation(clock / 50.0F));

        itemState.submit(poseStack, submitNodeCollector, state.lightCoords, 0, 0);
        poseStack.popPose();
    }

    @Override
    public void extractRenderState(EnchantingTableBlockEntity blockEntity, EnchantTableRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        if (!(blockEntity instanceof IEnchantingTableInventory inventory)) return;

        ItemStack tool = inventory.immersive$getItem(EnchantingTableMenu.Slots.TOOL.id());
        if (tool.isEmpty()) {
            itemState.clear();
            return;
        }

        //Skip drawing while the book is shut and not animating
        if (blockEntity.open <= 0.0F && blockEntity.oOpen <= 0.0F) {
            itemState.clear();
            return;
        }

        renderFloatingTool(blockEntity, tool, state, partialTicks);
    }

    private void renderFloatingTool(EnchantingTableBlockEntity blockEntity, ItemStack stack, EnchantTableRenderState state, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        mc.getItemModelResolver().updateForTopItem(
                itemState,
                stack,
                ItemDisplayContext.GROUND,
                blockEntity.getLevel(),
                null,
                0);

        itemState.newLayer().setFoilType(ItemStackRenderState.FoilType.STANDARD);

        clock = blockEntity.time + partialTicks;
        openness = Mth.lerp(partialTicks, blockEntity.oOpen, blockEntity.open);
    }
}
