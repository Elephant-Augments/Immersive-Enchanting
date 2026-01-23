package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public final class TooltipExtensions {
    private static final List<TooltipDescriptionExtension> EXTENSIONS = new ArrayList<>();

    /**
     * Register a new TooltipDescriptionExtension that will be rendered in EnchantingNodeTooltips.
     * @param extension
     */
    public static void register(TooltipDescriptionExtension extension) {
        EXTENSIONS.add(extension);
    }

    public static void apply(EnchantingNodeTooltip parentTooltip,
                      DescriptionLayout descriptionLayout) {
        for(TooltipDescriptionExtension extension : EXTENSIONS) {
            try {
                extension.extendLayout(descriptionLayout, parentTooltip);
            } catch (Exception e) {
                ImmersiveEnchanting.LOGGER.error("Tooltip extension failed: {}", extension.getClass().getName(), e);
            }
        }
    }
}
