package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.api.node.NodeData;
import net.minecraft.resources.Identifier;

public record ModFilterNodeData(String modid) {

    public static final String ALL_MODS = "all";
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "mod_filter");

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

}
