package me.alfie.immersiveenchanting.api.description;

import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipDescription;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DescriptionLayout {
    private static final Logger log = LoggerFactory.getLogger(DescriptionLayout.class);
    protected final List<DescriptionLine> lines = new ArrayList<>();
    protected final int lineSpace;

    /**Add extra padding to the width of the description layout to expand the box manually, useful for descriptions using item rendering.*/
    public int widthPadding = 0;

    public DescriptionLayout(TooltipDescription tooltipDescription) {
        lineSpace = Minecraft.getInstance().font.lineHeight;
    }

    public void insertLine(int lineNumber, DescriptionLine line) {
        while(lines.size() < lineNumber) {
            lines.add(new DescriptionLine() {
                @Override
                public void render(GuiGraphics graphics, int lineX, int lineY, double mouseX, double mouseY) {

                }
            });
        }

        lines.add(lineNumber, line);
    }

    public void removeLine(int lineNumber) {
        lines.remove(lineNumber);
    }

    /**
     * Clear the current layout.
     */
    public void clear() {
        lines.clear();
    }

    /**
     * Draw the lines in this layout.
     * @param graphics
     * @param startX The start position to render lines at
     * @param startY The start position to render lines at
     */
    public void render(GuiGraphics graphics, int startX, int startY, double mouseX, double mouseY) {
        int yOffset = 0;

        for (DescriptionLine line : lines) {
            line.render(graphics, startX, startY + yOffset, mouseX, mouseY);
            yOffset += lineSpace;
        }
    }



    /**
     * Return the longest string contained in the layout.
     * @return
     */
    private String getLongestString() {
        Component longest = Component.empty();
        for(DescriptionLine line : lines) {
            Component lineText = line.getText();

            if(lineText.getString().length() > longest.getString().length()) {
                longest = lineText;
            }
        }
        return longest.getString();
    }

    /**
     * Get the height of all the lines put together + any spacing.
     * @return
     */
    public int getRenderedHeight() {
        final int padding = 4;
        return (lines.size()+1) * lineSpace + padding;
    }

    public int getRenderedWidth() {
        final int padding = 8;
        return Minecraft.getInstance().font.width(getLongestString()) + padding + widthPadding;
    }
}
