package me.alfie.immersiveenchanting.api.filter.internal;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.EnchantmentFilterSelection;
import me.alfie.immersiveenchanting.api.filter.GlobalFilterBranch;
import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import me.alfie.immersiveenchanting.api.node.ItemIcon;
import me.alfie.immersiveenchanting.api.node.internal.FilterNodeData;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/** Built-in global filter: One branch showing all unlocked (non-isolated) enchantments. */
public final class UnlockedFilterBranch implements GlobalFilterBranch {

    public static final ResourceId ID = new ResourceId(ImmersiveEnchanting.MODID, "global/unlocked");

    @Override
    public ResourceId id() {
        return ID;
    }

    @Override
    public void addPickerBranches(BuildBranchesEvent event, List<Holder<Enchantment>> applicableEnchantments) {
        EnchantmentFilterSelection selection = event.getCanvas().screen().enchantmentFilterSelection();
        GlobalFilterBranchSupport.buildFilterBranch(
            event,
            GlobalFilterBranchSupport.createPickerBranchId(FilterNodeData.UNLOCKED_ONLY),
            Component.translatable("immersiveenchanting.mod_filter.unlocked"),
            NodeTier.ADVANCED,
            new ItemIcon(new ItemStack(Items.CHISELED_BOOKSHELF)),
            selection instanceof EnchantmentFilterSelection.Unlocked,
            FilterNodeData.create(EnchantmentFilterSelection.unlocked())
        );
    }
}
