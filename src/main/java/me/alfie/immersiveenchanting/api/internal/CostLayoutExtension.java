package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.datapack.cost.EnchantmentCost;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNode;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CostLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip) {
        //Only apply for EnchantingNodeTooltips
        if(parentTooltip instanceof EnchantingNodeTooltip enchantingNodeTooltip) {
            if (enchantingNodeTooltip.node instanceof EnchantingNode enchantingNode) {
                enchantingNodeTooltip.setCurrentRenderedCost(
                        EnchantingNodeTooltip.getCycledElement(enchantingNodeTooltip.getValidCosts(), 700));
                ItemStack stackToRender = enchantingNodeTooltip.getCurrentRenderedCost().asItemStack();

                CostEntry renderedCost = enchantingNodeTooltip.getCurrentRenderedCost();

                if(renderedCost.getCostItemTag().isPresent()) {
                    String itemTag = renderedCost.getCostItemTag().get().itemTag();
                    enchantingNodeTooltip.stackDescriptionComponents.set(0, Component.translatable("gui.immersiveenchanting.accepts_any_tag", itemTag)
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }

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
                            if (stackToRender.is(Items.AIR) || stackToRender.isEmpty()) {
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
                                        stackToRender,
                                        costStackPos.x,
                                        costStackPos.y);
                                graphics.renderItemDecorations(Minecraft.getInstance().font,
                                        stackToRender,
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

                if(renderedCost.xpLevels() > 0 && enchantingNode.isBranchUnlocked && !enchantingNode.isObtained()) {
                    description.insertLine(2, new DescriptionLine() {
                        @Override
                        public void draw(GuiGraphics graphics, int lineX, int lineY) {
                            //Draw label
                            graphics.drawString(Minecraft.getInstance().font,
                                    getText(),
                                    lineX,
                                    lineY + 4, //Offset to centre text with cost stack
                                    0xFFFFFF);

                            Vector2i levelLabelPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);

                            //Draw XP sprite
                            graphics.blit(
                                    EnchantingTableScreen.XP_LEVEL_SPRITE,
                                    levelLabelPos.x,
                                    lineY,
                                    0, 0, 16, 16, 16, 16);

                            //Draw XP level number
                            graphics.drawString(
                                    Minecraft.getInstance().font,
                                    Component.literal(String.valueOf(renderedCost.xpLevels())),
                                    levelLabelPos.x + 12,
                                    lineY + 4,
                                    0xC8FF8F
                            );
                        }

                        @Override
                        public @NotNull Component getText() {
                            Component label = Component.translatable("gui.immersiveenchanting.cost.xp")
                                    .withStyle(ChatFormatting.GRAY);
                            return label;
                        }
                    });
                }
            }
        }
    }


}
