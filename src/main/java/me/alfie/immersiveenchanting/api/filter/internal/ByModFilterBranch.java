package me.alfie.immersiveenchanting.api.filter.internal;

import me.alfie.alfinolib.datapacks.client.ClientDatapackManager;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.EnchantmentFilterSelection;
import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.filter.GlobalFilterBranch;
import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import me.alfie.immersiveenchanting.api.node.ItemIcon;
import me.alfie.immersiveenchanting.api.node.internal.FilterNodeData;
import me.alfie.immersiveenchanting.datapack.mod_icons.ModIconsDatapack;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Built-in global filter: One picker branch per mod namespace.
 */
public final class ByModFilterBranch implements GlobalFilterBranch {

    public static final ResourceId ID = new ResourceId(ImmersiveEnchanting.MODID, "global/by_mod");

    @Override
    public ResourceId id() {
        return ID;
    }

    @Override
    public void addPickerBranches(BuildBranchesEvent event, List<Holder<Enchantment>> applicableEnchantments) {
        EnchantmentFilterSelection selection = event.getCanvas().screen().enchantmentFilterSelection();

        Set<String> modids = new TreeSet<>();

        for(Holder<Enchantment> enchantmentHolder : applicableEnchantments) {
            if (FilterBranches.isInAnyIsolatedTag(enchantmentHolder)) {
                continue;
            }
            String modid = enchantmentHolder.getKey().location().getNamespace();
            modids.add(modid);
        }

        for(String modid : modids) {
            Component title = Component.literal(ImmersiveEnchanting.getModName(modid));
            boolean selected = selection instanceof EnchantmentFilterSelection.ModNamespace mod
                    && Objects.equals(mod.modid(), modid);
            GlobalFilterBranchSupport.buildFilterBranch(
                event,
                GlobalFilterBranchSupport.createPickerBranchId(modid),
                title,
                NodeTier.BASIC,
                new ItemIcon(ClientDatapackManager.get(ModIconsDatapack.KEY)
                        .getAsItemStack(modid)),
                selected,
                FilterNodeData.create(EnchantmentFilterSelection.mod(modid))
            );
        }
    }
}
