package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.internal.EnchantingLayoutExtension;
import me.alfie.immersiveenchanting.api.internal.ReplicateLayoutExtension;
import me.alfie.immersiveenchanting.api.internal.TransmuteLayoutExtension;
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
    public static void registerInternalTooltipDescriptions() {
        //Using the API hooks internally here to add text/custom rendering into the description box.
        TooltipDescriptionExtensions.register(new EnchantingLayoutExtension());
        TooltipDescriptionExtensions.register(new TransmuteLayoutExtension());
        TooltipDescriptionExtensions.register(new ReplicateLayoutExtension());
    }
}
