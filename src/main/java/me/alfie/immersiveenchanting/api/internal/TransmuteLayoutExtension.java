package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNode;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
                            Component.translatable("gui.immersiveenchanting.transmute_description").getString() :
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
                                    label = label.copy().withStyle(ChatFormatting.GREEN);
                                } else {
                                    label = label.copy().withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
                                }

                                return label;
                            }
                        });
                    }

                    //Cost label if unlocked
                    if(transmuteNode.canTransmute()) {
                        description.insertLine(lineCount, new DescriptionLine() {
                            @Override
                            public void draw(GuiGraphics graphics, int lineX, int lineY) {
                                graphics.drawString(Minecraft.getInstance().font,
                                        getText(),
                                        lineX,
                                        lineY + 4, //Offset to centre text with cost stack
                                        0xFFFFFF);

                                Vector2i spritePos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText().getString()), lineY);
                                graphics.blit(EnchantingTableScreen.LEVEL_SPRITE, spritePos.x, spritePos.y, 0, 0, 16, 16, 16, 16);
                            }

                            @Override
                            public @NotNull Component getText() {
                                return Component.translatable("gui.immersiveenchanting.cost").withStyle(ChatFormatting.GRAY);
                            }
                        });
                    }

                }
            }
        }
}

