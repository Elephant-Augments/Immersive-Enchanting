package me.alfie.immersiveenchanting.api.description;

import me.alfie.immersiveenchanting.api.description.internal.lines.FuelsLine;
import me.alfie.immersiveenchanting.api.description.internal.lines.LevelsLine;
import me.alfie.immersiveenchanting.api.description.internal.lines.MaterialsLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DescriptionHelper {

    public static final int DEFAULT_LINE_WIDTH = 32;

    public static void text(GuiGraphicsExtractor graphics, Component component, int x, int y) {
        graphics.text(Minecraft.getInstance().font, component, x, y, Color.WHITE.getRGB());
    }

    /**
     * Inserts cost lines (materials, fuel, XP levels) into {@code description} starting at
     * {@code lineStart}, skipping any cost component that is empty (AIR stack / 0 levels).
     * Each item-stack cost occupies two slots to leave room for the rendered item icon.
     */
    public static void insertCostLines(NodeTooltip tooltip, DescriptionLayout description, int lineStart){
        int lineNumber = lineStart;
        if(!tooltip.screen().enchantmentCostRenderer().getCurrentRenderedCost().stack().is(Items.AIR)) {
            description.insertLine(lineNumber, new MaterialsLine(tooltip));
            lineNumber += 2;
        }

        if(!tooltip.screen().enchantmentCostRenderer().getCurrentRenderedFuel().stack().is(Items.AIR)) {
            description.insertLine(lineNumber, new FuelsLine(tooltip));
            lineNumber += 2;
        }

        if(tooltip.screen().enchantmentCostRenderer().getCurrentRenderedCost().xpLevels() > 0) description.insertLine(lineNumber, new LevelsLine(tooltip));
    }

    /**
     * Splits {@code component} into word-wrapped chunks of at most {@code lineSize} characters
     * and inserts each chunk as a separate {@link DescriptionLine} starting at {@code lineStart}.
     * The original component's style is preserved on every wrapped line.
     * @return The number of lines inserted
     */
    public static int lineWrapComponent(Component component, int lineSize, DescriptionLayout description, int lineStart) {
        List<String> textLines = chunkString(component.getString(), lineSize);
        int totalLines = 0;
        for (int i = 0; i < textLines.size(); i++) {
            i += lineStart;
            final int finalI = i;

            description.insertLine(i, new DescriptionLine() {
                @Override
                public void render(GuiGraphicsExtractor graphics, int lineX, int lineY, double mouseX, double mouseY) {
                    text(graphics, getText(), lineX, lineY);
                }

                @Override
                public @NotNull Component getText() {
                    return Component.literal(textLines.get(finalI)).withStyle(component.getStyle());
                }
            });

            totalLines++;
        }

        return totalLines;
    }

    /**
     * Split a string into chunks.
     * @param text
     * @return
     */
    private static List<String> chunkString(String text, int chunkSize) {
        java.util.List<String> parts = new ArrayList<>();

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

}
