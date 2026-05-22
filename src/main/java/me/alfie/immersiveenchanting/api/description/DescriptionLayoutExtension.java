package me.alfie.immersiveenchanting.api.description;

import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;

public interface DescriptionLayoutExtension {
    /**
     * Called each time a {@link NodeTooltip} rebuilds its layout.
     * Implementations should insert any {@link DescriptionLine}s they want to contribute
     * into {@code description}. The layout is cleared before this is called, so there is
     * no need to guard against duplicate lines across rebuilds.
     */
    void extendLayout(DescriptionLayout description, NodeTooltip tooltip);
}
