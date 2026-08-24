package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.EnchantmentFilterSelection;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;


/**
 * Payload for hold-to-filter picker nodes (global and isolated).
 *
 * <p>On click, applies {@link #selection()} via
 * {@link EnchantingTableScreen#selectEnchantmentFilter(EnchantmentFilterSelection)}.</p>
 */
public record FilterNodeData(EnchantmentFilterSelection selection) implements NodePayload {

    /** Picker branch path for the All filter ({@code mod_filter/all}). */
    public static final String ALL_MODS = "all";
    /** Picker branch path for the Unlocked filter ({@code mod_filter/unlocked}). */
    public static final String UNLOCKED_ONLY = "unlocked";

    public static final ResourceId TYPE = new ResourceId(ImmersiveEnchanting.MODID, "filter");

    @Override
    public ResourceId type() {
        return TYPE;
    }

    public static NodeData<FilterNodeData> create(EnchantmentFilterSelection selection) {
        return new NodeData<>(
                TYPE,
                new FilterNodeData(selection),
                (data, context) -> context.screen().selectEnchantmentFilter(data.selection())
        );
    }
}
