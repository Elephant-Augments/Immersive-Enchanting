package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;

import java.util.ArrayList;
import java.util.List;

public final class TooltipDescriptionExtensions {
    private static final List<DescriptionLayoutExtension> EXTENSIONS = new ArrayList<>();

    /**
     * Register a new DescriptionLayoutExtension that will be rendered in EnchantingNodeTooltips.
     * @param extension
     */
    public static void register(DescriptionLayoutExtension extension) {
        EXTENSIONS.add(extension);
    }

    /**
     * Call extendLayout on all registered DescriptionLayoutExtensions
     * @param parentTooltip
     * @param descriptionLayout
     */
    public static void apply(NodeTooltip parentTooltip,
                             DescriptionLayout descriptionLayout) {
        for(DescriptionLayoutExtension extension : EXTENSIONS) {
            try {
                extension.extendLayout(descriptionLayout, parentTooltip);
            } catch (Exception e) {
                ImmersiveEnchanting.LOGGER.error("Tooltip extension failed: {}", extension.getClass().getName(), e);
            }
        }
    }

    //Register internal description layouts.
    public void registerInternalDescriptions() {

    }
}
