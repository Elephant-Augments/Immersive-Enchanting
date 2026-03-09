package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DescriptionLayout {
    protected final List<DescriptionLine> lines = new ArrayList<>();
    protected final int lineSpace;
    private final int MAX_LINE_LENGTH = 64;

    public DescriptionLayout(TooltipDescription tooltipDescription) {
        lineSpace = Minecraft.getInstance().font.lineHeight;
    }

    /**
     * Split a string into chunks.
     * @param text
     * @return
     */
    public static List<String> chunkString(String text, int chunkSize) {
        List<String> parts = new ArrayList<>();

        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            // If adding this word would exceed the limit, flush the current chunk
            if (current.length() > 0 &&
                    current.length() + 1 + word.length() > chunkSize) {

                parts.add(current.toString());
                current.setLength(0);
            }

            // Append word (with space if needed)
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(word);
        }

        // Add remainder
        if (current.length() > 0) {
            parts.add(current.toString());
        }

        return parts;
    }


    /**
     * Get the height of all the lines put together + any spacing.
     * @return
     */
    public int getRenderedHeight() {
        return (lines.size()+1) * lineSpace;
    }

    public void insertLine(int lineNumber, DescriptionLine line) {
        while(lines.size() < lineNumber) {
            //Add empty line
            lines.add(new DescriptionLine() {
                @Override
                public void draw(GuiGraphics graphics, int lineX, int lineY) {

                }

                @Override
                public @NotNull Component getText() {
                    return Component.empty();
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
    protected void draw(GuiGraphics graphics, int startX, int startY) {
        int yOffset = 0;

        for (DescriptionLine line : lines) {
            line.draw(graphics, startX, startY + yOffset);

            yOffset += lineSpace; //Move to next line
        }
    }

    /**
     * Return the longest string contained in the layout.
     * @return
     */
    public String getLongestString() {
        Component longest = Component.empty();
        for(DescriptionLine line : lines) {
            Component lineText = line.getText();
            lineText = (lineText == null) ? Component.empty() : lineText; //Defend against null
            if(lineText.getString().length() > longest.getString().length()) {
                longest = lineText;
            }
        }
        return longest.getString();
    }
}
