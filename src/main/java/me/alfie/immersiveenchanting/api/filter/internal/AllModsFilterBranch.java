package me.alfie.immersiveenchanting.api.filter.internal;

import me.alfie.alfinolib.datapacks.client.ClientDatapackManager;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.EnchantmentFilterSelection;
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

/** Built-in global filter: One global branch showing all (non-isolated) enchantments. */
public final class AllModsFilterBranch implements GlobalFilterBranch {

    public static final ResourceId ID = new ResourceId(ImmersiveEnchanting.MODID, "global/all");

    @Override
    public ResourceId id() {
        return ID;
    }

    @Override
    public void addPickerBranches(BuildBranchesEvent event, List<Holder<Enchantment>> applicableEnchantments) {
        EnchantmentFilterSelection selection = event.getCanvas().screen().enchantmentFilterSelection();
        GlobalFilterBranchSupport.buildFilterBranch(
            event,
            GlobalFilterBranchSupport.createPickerBranchId(FilterNodeData.ALL_MODS),
            Component.translatable("immersiveenchanting.mod_filter.all"),
            NodeTier.ELITE,
            new ItemIcon(ClientDatapackManager.get(ModIconsDatapack.KEY)
                    .getAsItemStack(FilterNodeData.ALL_MODS)),
            selection instanceof EnchantmentFilterSelection.All,
            FilterNodeData.create(EnchantmentFilterSelection.all())
        );
    }
}
