package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Functions to create branches pre-filled with nodes.
 */
public class BranchFactory {

    public static List<NodeBranch> buildBranches(ItemStack stack,
                                                 RegistryAccess registryAccess,
                                                 Canvas canvas) {
        return buildEnchantingBranches(stack, registryAccess, canvas);
    }

    private static List<Holder<Enchantment>> getApplicableEnchantments(ItemStack stack,
                                                                       RegistryAccess registryAccess) {
        List<Holder<Enchantment>> enchantments = EnchantmentUtil.idsToHolders(EnchantmentCostRegistry.getAllEnchantmentIds(), registryAccess);
        List<Holder<Enchantment>> applicableEnchantments = new ArrayList<>();

        for(Holder<Enchantment> enchantment : enchantments) {
            Set<Holder<Enchantment>> itemEnchantments = new HashSet<>(stack.getTagEnchantments().keySet());
            itemEnchantments.remove(enchantment);
            if(!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantment)) continue;
            if(stack.supportsEnchantment(enchantment)) applicableEnchantments.add(enchantment);
        }
        return applicableEnchantments;
    }

    /**
     * Gets all enchantments from the EnchantmentCostRegistry and creates a NodeBranch for each one.
     * @param stack
     * @param registryAccess
     * @return
     */
    private static List<NodeBranch> buildEnchantingBranches(ItemStack stack,
                                                            RegistryAccess registryAccess,
                                                            Canvas canvas) {
        List<NodeBranch> result = new ArrayList<>();
        List<Holder<Enchantment>> applicableEnchantments = getApplicableEnchantments(stack, registryAccess);

        List<Float> angles = generateBranchAngles(applicableEnchantments.size());

        for (int i = 0; i < applicableEnchantments.size(); i++) {
            Holder<Enchantment> enchantment = applicableEnchantments.get(i);
            int equippedLevel = stack.getEnchantmentLevel(enchantment);

            result.add(buildEnchantingBranch(enchantment, equippedLevel, angles.get(i), canvas));
        }
        return result;
    }

    private static NodeBranch buildEnchantingBranch(Holder<Enchantment> enchantment,
                                                    int equippedLevel,
                                                    float angle,
                                                    Canvas canvas) {
        List<Node> nodes = new ArrayList<>();

        int maxLevel = EnchantmentCostRegistry.get(EnchantmentUtil.toId(enchantment)).levelCosts().maxLevel();
        for (int enchantmentLevel = 0; enchantmentLevel < maxLevel; enchantmentLevel++) {
            NodeState state = equippedLevel > enchantmentLevel ? NodeState.OBTAINED : NodeState.UNOBTAINED;
            NodeTier tier = enchantmentLevel+1 == maxLevel ? NodeTier.ELITE : NodeTier.BASIC;

            nodes.add(new Node(EnchantmentUtil.toId(enchantment), enchantmentLevel+1, canvas, state, tier));
        }

        return new NodeBranch(canvas, nodes, angle);
    }

    /**
     * Generate a list of angles based on the total number of branches.
     *
     * @param totalBranches
     * @return
     */
    private static ArrayList<Float> generateBranchAngles(int totalBranches) {
        // No more than 16 branches
        ArrayList<Float> angles = new ArrayList<>();
        for (int i = 0; i < totalBranches; i++) {
            float angle = (float) (i * 2 * Math.PI / totalBranches); // evenly spaced
            angles.add(angle);
        }
        return angles;
    }
}
