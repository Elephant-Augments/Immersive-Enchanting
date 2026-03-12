package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node;

import me.alfie.immersiveenchanting.compat.ModCheck;
import me.alfie.immersiveenchanting.compat.ModCompat;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedNBT;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.enchanting.EnchantingNodeBranch;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.replicate.ReplicateNodeBranch;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.transmute.TransmuteNodeBranch;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BranchFactory {

    /**
     * Finds which enchantments are applicable for an item stack , generates branch angles, and creates the branches.
     *
     * @param stack
     */
    public static List<NodeBranch> buildEnchantingNodeBranches(ItemStack stack, EnchantingTableScreen screen) {
        List<NodeBranch> branches = new ArrayList<>();

        //Step 1. Find which enchantments are applicable.
        List<Holder<Enchantment>> applicableEnchantments = new ArrayList<>();
        Set<Holder<Enchantment>> unlockedEnchantments = screen.getMenu().getUnlockedEnchantments();
        List<Holder.Reference<Enchantment>> allEnchantments = EnchantmentUtil.getAllEnchantments(screen.player.level());

        List<Holder<Enchantment>> allEnchantmentsSorted = new ArrayList<>(allEnchantments);
        allEnchantmentsSorted.sort(Comparator.comparing(
                holder -> holder.unwrapKey().get().location().toString()));

        //Iterate through the enchantment registry, see if the item support the enchantment.
        for (Holder<Enchantment> enchantmentHolder : allEnchantmentsSorted) {
            Set<Enchantment> itemEnchantments = new HashSet<>(stack.getAllEnchantments().keySet());
            itemEnchantments.remove(enchantmentHolder.get()); //Ignore the enchantment we're trying to check for
            if (!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantmentHolder.get())) continue;
            if (enchantmentHolder.get().canEnchant(stack)) applicableEnchantments.add(enchantmentHolder);
        }

        List<Float> angles = generateBranchAngles(applicableEnchantments.size());

        int i = 0;
        for (Holder<Enchantment> enchantmentHolder : applicableEnchantments) {
            boolean isUnlocked = unlockedEnchantments.contains(enchantmentHolder);
            AtomicInteger enchantmentLevel = new AtomicInteger();
            enchantmentLevel.set(stack.getItem().getEnchantmentLevel(stack, enchantmentHolder.value()));

            branches.add(new EnchantingNodeBranch(
                    screen,
                    angles.get(i),
                    enchantmentHolder,
                    enchantmentLevel.get(),
                    isUnlocked,
                    screen.player));
            i++;
        }
        return branches;
    }

    /**
     * Special method to build the upgrade node for ancient book enchantment re-roll.
     */
    public static List<NodeBranch> buildAncientBookBranch(ItemStack currentItemStack, EnchantingTableScreen screen) {
        List<NodeBranch> branches = new ArrayList<>();
        Level level = screen.player.level();

        Set<Holder<Enchantment>> unlockedEnchantments = screen.getMenu().getUnlockedEnchantments();

        ResourceKey<Enchantment> enchantmentResourceKey = AncientBook.getStoredEnchantment(currentItemStack);
        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.getEnchantmentHolder(level.registryAccess(), enchantmentResourceKey).get();

        Set<Holder<Enchantment>> ancientBookEnchantment = new HashSet<>(Set.of(enchantmentHolder));

        //Check if the enchantment stored in this ancient book is also unlocked (in the bookshelf)
        boolean canTransmute = !Collections.disjoint(unlockedEnchantments, ancientBookEnchantment);
        boolean isBookReplicated = ReplicatedNBT.isReplicated(currentItemStack);

        List<Float> angles = generateBranchAngles(2);

        if(ServerConfig.isAllowTransmute()) {
            branches.add(new TransmuteNodeBranch(
                    screen,
                    angles.get(0),
                    canTransmute,
                    isBookReplicated));
        }

        if(ServerConfig.isAllowReplicate()) {
            branches.add(new ReplicateNodeBranch(
                    screen,
                    angles.get(1)));
        }

        return branches;
    }


    /**
     * Generate a list of angles based on the total number of branches.
     *
     * @param totalBranches
     * @return
     */
    public static ArrayList<Float> generateBranchAngles(int totalBranches) {
        // No more than 16 branches
        ArrayList<Float> angles = new ArrayList<>();
        for (int i = 0; i < totalBranches; i++) {
            float angle = (float) (i * 2 * Math.PI / totalBranches); // evenly spaced
            angles.add(angle);
        }
        return angles;
    }
}
