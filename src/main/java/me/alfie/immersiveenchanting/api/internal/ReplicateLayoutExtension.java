package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.internal.cost.LevelsDescriptionLine;
import me.alfie.immersiveenchanting.api.internal.cost.MaterialsDescriptionLine;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.replicate.ReplicateNode;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.replicate.ReplicateNodeTooltip;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip.DescriptionLayout;

public class ReplicateLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip) {
        if(parentTooltip instanceof ReplicateNodeTooltip replicateNodeTooltip) {
            if(parentTooltip.node instanceof ReplicateNode replicateNode) {
                //Description label
                /*
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
                */

                //Cost Layout
                replicateNodeTooltip.setCurrentRenderedCost(NodeTooltip.getCycledElement(replicateNodeTooltip.getValidCosts(), ClientConfig.getItemCarouselSpeed()));
                description.insertLine(0, new MaterialsDescriptionLine(replicateNodeTooltip));

                if(replicateNodeTooltip.getCurrentRenderedCost().xpLevels() > 0) {
                    description.insertLine(2, new LevelsDescriptionLine(replicateNodeTooltip));
                }

            }
        }
    }
}
