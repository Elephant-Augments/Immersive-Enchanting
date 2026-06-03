package me.alfie.immersiveenchanting.api.description;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.description.internal.EnchantLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.ModFilterLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.ReplicateLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.TransmuteLayoutExtension;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;

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
        ImmersiveEnchanting.LOGGER.debug("Successfully registered DescriptionLayoutExtension {}", extension);
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

    /**
     * Clear the layout and re-run all registered extensions. Use this when the node state
     * has changed and the entire description needs to be regenerated from scratch.
     */
    public static void rebuild(NodeTooltip parentTooltip,
                               DescriptionLayout descriptionLayout) {
        descriptionLayout.clear();
        apply(parentTooltip, descriptionLayout);
    }

    /** Registers the built-in layout extensions via the {@link RegisterDescriptionLayoutEvent}. */
    public static void registerInternalTooltipDescriptions(RegisterDescriptionLayoutEvent event) {
        event.register(new EnchantLayoutExtension());
        event.register(new TransmuteLayoutExtension());
        event.register(new ReplicateLayoutExtension());
        event.register(new ModFilterLayoutExtension());
    }
}
