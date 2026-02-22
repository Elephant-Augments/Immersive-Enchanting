package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNode;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNodeTooltip;
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

public class ReplicateLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip) {
        if(parentTooltip instanceof ReplicateNodeTooltip replicateNodeTooltip) {
            if(parentTooltip.node instanceof ReplicateNode replicateNode) {
                //Description label
                String text = Component.translatable("gui.immersiveenchanting.replicate_hint").getString();
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
                            Component label = Component.literal(textChunks.get(finalI)).withStyle(ChatFormatting.GRAY);
                            return label;
                        }
                    });
                }

                //Cost label
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

                        int xOffset = 16+4;

                        //replicateNodeTooltip.setCostStackPos(bcostStackPos.x, costStackPos.y);
                        graphics.renderItem(
                                new ItemStack(Items.WRITABLE_BOOK),
                                spritePos.x + xOffset,
                                spritePos.y);
                        graphics.renderItemDecorations(Minecraft.getInstance().font,
                                new ItemStack(Items.WRITABLE_BOOK),
                                spritePos.x + xOffset,
                                spritePos.y);
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
