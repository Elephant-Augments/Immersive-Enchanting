package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.datapack.CostLeaf;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNode;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;

public class CostLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip) {
        //Only apply for EnchantingNodeTooltips
        if(parentTooltip instanceof EnchantingNodeTooltip enchantingNodeTooltip) {
            if (enchantingNodeTooltip.node instanceof EnchantingNode enchantingNode) {

                enchantingNodeTooltip.setCurrentRenderedCost(
                        EnchantingNodeTooltip.getCycledElement(enchantingNodeTooltip.getValidCosts(), 1000));

                description.insertLine(0, new DescriptionLine() {
                    @Override
                    public void draw(GuiGraphics graphics, int lineX, int lineY) {
                        //Draw label
                        graphics.drawString(Minecraft.getInstance().font,
                                getText(),
                                lineX,
                                lineY + 4, //Offset to centre text with cost stack
                                0xFFFFFF);

                        //Draw cost stack or "Free" if no item cost defined.
                        if (enchantingNode.isBranchUnlocked && !enchantingNode.isObtained()) {
                            ItemStack costStack = enchantingNodeTooltip.getCurrentRenderedCost().asItemStack();
                            if (costStack.is(Items.AIR) || costStack.isEmpty()) {
                                graphics.drawString(
                                        Minecraft.getInstance().font,
                                        Component.translatable("gui.immersiveenchanting.cost_free"),
                                        lineX,
                                        lineY,
                                        ChatFormatting.DARK_AQUA.getColor()
                                );
                            } else {
                                Vector2i costStackPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);
                                enchantingNodeTooltip.setCostStackPos(costStackPos.x, costStackPos.y);
                                graphics.renderItem(
                                        enchantingNodeTooltip.getCurrentRenderedCost().asItemStack(),
                                        costStackPos.x,
                                        costStackPos.y);
                                graphics.renderItemDecorations(Minecraft.getInstance().font,
                                        enchantingNodeTooltip.getCurrentRenderedCost().asItemStack(),
                                        costStackPos.x,
                                        costStackPos.y);
                            }
                        }
                    }

                    @Override
                    public @NotNull Component getText() {
                        Component label;
                        if (enchantingNode.isBranchUnlocked) {
                            label = enchantingNode.isObtained() ?
                                    Component.translatable("gui.immersiveenchanting.equipped").withStyle(ChatFormatting.LIGHT_PURPLE) :
                                    Component.translatable("gui.immersiveenchanting.cost").withStyle(ChatFormatting.GRAY);
                        } else {
                            label = Component.translatable("gui.immersiveenchanting.locked_enchantment_hint")
                                    .withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.GRAY);
                        }
                        return label;
                    }
                });

            }
        }
    }


}
