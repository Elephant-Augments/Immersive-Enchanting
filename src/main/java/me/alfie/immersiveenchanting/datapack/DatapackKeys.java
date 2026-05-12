package me.alfie.immersiveenchanting.datapack;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackKey;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.mod_icons.ModIconsMap;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;

/**Internal DatapackKeys*/
public final class DatapackKeys {

    public static final DatapackKey<CostRegistry> COST = new DatapackKey<>(ImmersiveEnchanting.MODID, "enchantment_costs");
    public static final DatapackKey<NodeSoundMap> NODE_SOUNDS = new DatapackKey<>(ImmersiveEnchanting.MODID, "sounds");
    public static final DatapackKey<ModIconsMap> MOD_ICONS = new DatapackKey<>(ImmersiveEnchanting.MODID, "mod_icons");
}
