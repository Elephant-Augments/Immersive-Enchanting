package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.internal.cost.LevelsDescriptionLine;
import me.alfie.immersiveenchanting.api.internal.cost.MaterialsDescriptionLine;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNode;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;

public class TransmuteLayoutExtension implements DescriptionLayoutExtension {

        @Override
        public void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip) {
            if(parentTooltip instanceof TransmuteNodeTooltip transmuteNodeTooltip) {
                if(parentTooltip.node instanceof TransmuteNode transmuteNode) {
                    //Description label
                    String text = transmuteNode.canTransmute() ?
                            Component.EMPTY.getString() :
                            Component.translatable("gui.immersiveenchanting.transmute_hint").getString();

                    if(transmuteNode.isBookReplicated()) {
                        text = Component.translatable("gui.immersiveenchanting.not_transmutable").getString();
                    }

                    List<String> textChunks = DescriptionLayout.chunkString(text, 32);
                    int lineCount = 0;
                    for (int i = 0; i < textChunks.size(); i++) {
                        lineCount++;
                        int finalI = i;
                        description.insertLine(i, new DescriptionLine() {
                            @Override
                            public void draw(GuiGraphics graphics, int lineX, int lineY) {
                                //Draw label
                                graphics.drawString(Minecraft.getInstance().font,
                                        getText(),
                                        lineX,
                                        lineY,
                                        0xFFFFFF);
                            }

                            @Override
                            public @NotNull Component getText() {
                                Component label = Component.literal(textChunks.get(finalI));
                                if(transmuteNode.canTransmute()) {
                                    //label = label.copy().withStyle(ChatFormatting.GREEN);
                                    label = Component.empty();
                                } else {
                                    label = label.copy().withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
                                }

                                return label;
                            }
                        });
                    }

                    //Cost Layout
                    if(transmuteNode.canTransmute()) {
                        transmuteNodeTooltip.setCurrentRenderedCost(NodeTooltip.getCycledElement(transmuteNodeTooltip.getValidCosts(), ClientConfig.getItemCarouselSpeed()));
                        description.insertLine(0, new MaterialsDescriptionLine(transmuteNodeTooltip));

                        if(transmuteNodeTooltip.getCurrentRenderedCost().xpLevels() > 0) {
                            description.insertLine(2, new LevelsDescriptionLine(transmuteNodeTooltip));
                        }
                    }

                }
            }
        }
}

