package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;


public record ModFilterNodeData(String modid) implements NodePayload {

    public static final String ALL_MODS = "all";
    public static final String UNLOCKED_ONLY = "unlocked";
    public static final String COSMETICS = "cosmetics";
    public static final ResourceId TYPE = new ResourceId(ImmersiveEnchanting.MODID, "mod_filter");
    @Override public ResourceId type() {
        return TYPE;
    }

    /**
     * Creates a {@link NodeData} for a mod-filter node.
     *
     * <p>On click, sets the shared view filter: {@link #ALL_MODS} shows every
     * non-cosmetic enchantment, {@link #UNLOCKED_ONLY} shows unlocked ones,
     * {@link #COSMETICS} shows entries in {@code #immersiveenchanting:cosmetics},
     * and any other value filters to the relevant mod id.</p>
     */
    public static NodeData<ModFilterNodeData> create(String modid) {
        return new NodeData<>(
                TYPE,
                new ModFilterNodeData(modid),
                (data, context) -> {
                    EnchantingTableScreen screen = context.screen();

                    if (ALL_MODS.equals(data.modid())) {
                        screen.selectModFromPicker(null);
                    } else if (UNLOCKED_ONLY.equals(data.modid())) {
                        screen.selectUnlockedFromPicker();
                    } else if (COSMETICS.equals(data.modid())) {
                        screen.selectModFromPicker(COSMETICS);
                    } else {
                        screen.selectModFromPicker(data.modid());
                    }
                }
        );
    }
}
