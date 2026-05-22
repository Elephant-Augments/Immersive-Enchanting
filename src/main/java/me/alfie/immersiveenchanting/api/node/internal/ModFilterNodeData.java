package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import net.minecraft.resources.ResourceLocation;

public record ModFilterNodeData(String modid) implements NodePayload {

    public static final String ALL_MODS = "all";
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "mod_filter");

    /**
     * Creates a {@link NodeData} for a mod-filter node.
     *
     * <p>On click, sets the enchanting tab's active mod filter to {@code modid}
     * (or clears it when {@code modid} is {@link #ALL_MODS}) and rebuilds the branch list.
     */
    public static NodeData<ModFilterNodeData> create(String modid) {
        return new NodeData<>(
                TYPE,
                new ModFilterNodeData(modid),
                (data, context) -> {
                    EnchantingTableScreen screen = context.screen();

                    String id = ALL_MODS.equals(data.modid()) ? null : data.modid();

                    screen.enchantingTab().setFilteredModid(id);
                    screen.rebuildBranches();
                }
        );
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }
}
