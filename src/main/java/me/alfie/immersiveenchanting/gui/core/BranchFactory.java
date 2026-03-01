package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.compat.ModCheck;
import me.alfie.immersiveenchanting.compat.ModCompat;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNodeBranch;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNodeBranch;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNodeBranch;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.config.ModConfig;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BranchFactory {

    /**
     * Finds which enchantments are applicable for an item stack , generates branch angles, and creates the branches.
     *
     * @param stack
     */
    public static List<NodeBranch> buildEnchantingNodeBranches(ItemStack stack, EnchantingTableScreen screen) {
        List<NodeBranch> branches = new ArrayList<>();

        //Step 1. Find which enchantments are applicable.
        List<Holder<Enchantment>> validEnchantments = new ArrayList<>(); //Set of all enchantments that can be applied
        Set<Holder<Enchantment>> unlockedEnchantments = screen.getMenu().getUnlockedEnchantments();

        Set<Holder<Enchantment>> allEnchantments = ImmersiveEnchanting.getEnchantmentRegistry(
                        screen.player.registryAccess())
                .asLookup()
                .listElements()
                .collect(Collectors.toSet());

        //Sort set into alphabetical order so that branches appear in the same order every time.
        List<Holder<Enchantment>> allEnchantmentsSorted = new ArrayList<>(allEnchantments);
        allEnchantmentsSorted.sort(Comparator.comparing(
                holder -> holder.getKey().location().toString()
        ));
        System.out.println(allEnchantmentsSorted);

        //Iterate through the enchantment registry, see if the item support the enchantment.
        for (Holder<Enchantment> enchantmentHolder : allEnchantmentsSorted) {
            //Skip cursed enchantments.
            if (enchantmentHolder.is(EnchantmentTags.CURSE)) {
                continue;
            }

            ResourceKey<Enchantment> enchantmentKey = enchantmentHolder.getKey();

            //Skip disabled enchantments, they won't appear in the table
            if (EnchantmentCostRegistry.getClientRegistry().getCostRegistry().containsKey(enchantmentKey)) {
                if(!EnchantmentCostRegistry.getClientRegistry().getCostRegistry().get(enchantmentKey).enabled) continue;
            }

            //If this enchantment isn't compatible with any enchantments already applied to the item, then skip.
            Set<Holder<Enchantment>> itemEnchantments = new HashSet<>(stack.getTagEnchantments().keySet());
            itemEnchantments.remove(enchantmentHolder); //Ignore the enchantment we're trying to check for

            if (!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantmentHolder)) {
                continue;
            }

            //Add enchantment
            if (stack.getItem().supportsEnchantment(stack, enchantmentHolder)) {
                validEnchantments.add(enchantmentHolder);
            }
        }

        // Step 2: Generate angles after filtering
        List<Float> angles = generateBranchAngles(validEnchantments.size());

        int i = 0;
        for (Holder<Enchantment> enchantmentHolder : validEnchantments) {
            //Check if the enchantmentResourceId is unlocked.
            boolean isUnlocked = unlockedEnchantments.contains(enchantmentHolder);
            AtomicInteger enchantmentLevel = new AtomicInteger();

            //Get the level of this enchantment
            enchantmentLevel.set(stack.getItem().getEnchantmentLevel(stack, enchantmentHolder));


            if(ModCheck.Mod.RELIQUARY.isLoaded()) {
                ModCompat.reliquaryMagicbaneFix(stack, enchantmentHolder, enchantmentLevel);
            }

            branches.add(new EnchantingNodeBranch(
                    screen,
                    angles.get(i),
                    enchantmentHolder,
                    enchantmentLevel.get(),
                    isUnlocked,
                    screen.player
            ));
            i++;
        }
        return branches;
    }

    /**
     * Special method to build the upgrade node for ancient book enchantment re-roll.
     */
    public static List<NodeBranch> buildAncientBookBranch(ItemStack currentItemStack, EnchantingTableScreen screen) {
        List<NodeBranch> branches = new ArrayList<>();

        //If the enchantment is already unlocked (i.e, there is an identical book in the chiseled bookshelf)
        //Reroll this ancient book to a new one (that isn't unlocked)

        //All ancient books that are already present
        Set<Holder<Enchantment>> unlockedEnchantments = screen.getMenu().getUnlockedEnchantments();

        //The enchantment for this ancient book
        Set<Holder<Enchantment>> ancientBookEnchantments = currentItemStack.get(DataComponents.STORED_ENCHANTMENTS).keySet();

        System.out.println(ancientBookEnchantments);
        System.out.println(unlockedEnchantments);

        //Check if the enchantment stored in this ancient book is also unlocked (in the bookshelf)
        boolean canTransmute = false;
        if(!Collections.disjoint(unlockedEnchantments, ancientBookEnchantments)) {
            canTransmute = true;
        }

        boolean isBookReplicated = ReplicatedDataComponent.isReplicated(currentItemStack);

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
