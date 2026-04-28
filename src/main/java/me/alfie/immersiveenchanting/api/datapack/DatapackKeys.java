package me.alfie.immersiveenchanting.api.datapack;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import net.minecraft.resources.Identifier;

import java.lang.invoke.CallSite;

/**Internal DatapackKeys*/
public final class DatapackKeys {

    public static final DatapackKey<CostRegistry> COST = new DatapackKey<>(ImmersiveEnchanting.MODID, "enchantment_costs");
    public static final DatapackKey<NodeSoundMap> NODE_SOUNDS = new DatapackKey<>(ImmersiveEnchanting.MODID, "sounds");
}
