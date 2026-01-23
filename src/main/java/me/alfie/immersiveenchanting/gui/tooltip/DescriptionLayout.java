package me.alfie.immersiveenchanting.gui.tooltip;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class DescriptionLayout {
    protected final List<DescriptionLine> lines = new ArrayList<>();
    protected final int lineSpace;

    public DescriptionLayout(TooltipDescription tooltipDescription) {
        lineSpace = tooltipDescription.parentTooltip.getFont().lineHeight;
    }

    /**
     * Get the height of all the lines put together.
     * @return
     */
    public int getTotalHeight() {
        return (lines.size()+1) * lineSpace;
    }

    /**
     * Append a new line to the description box. Use a lambda of DescriptionLine i.e {@code (graphics, x, y) -> {}}
     * @param line
     */
    public void appendNewLine(DescriptionLine line) {
        lines.add(line);
    }

    public void insertLine(int lineNumber, DescriptionLine line) {
        lines.add(lineNumber, line);
    }

    /**
     * Empty line, forces the next append to the next line - empty line acts as an empty space in the layout.
     */
    public void appendEmptyLine() {
        appendNewLine((graphics, lineX, lineY) -> {});
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
    public void draw(GuiGraphics graphics, int startX, int startY) {
        int yOffset = 0;

        for (DescriptionLine line : lines) {
            line.draw(graphics, startX, startY + yOffset);
            yOffset += lineSpace;
        }
    }
}
