package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.core.Holder;
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

    public static List<NodeBranch> buildBranches(ItemStack stack, CostRegistry costRegistry, Canvas canvas) {
        return buildEnchantingBranches(stack, costRegistry, canvas);
    }

    private static List<Holder<Enchantment>> getApplicableEnchantments(ItemStack stack, CostRegistry costRegistry) {
        List<Holder<Enchantment>> sortedEnchantments = EnchantmentUtil.sortByName(costRegistry.getAllEnchantmentHolders());

        List<Holder<Enchantment>> applicableEnchantments = new ArrayList<>();

        for(Holder<Enchantment> enchantment : sortedEnchantments) {
            Set<Holder<Enchantment>> itemEnchantments = new HashSet<>(stack.getTagEnchantments().keySet());
            itemEnchantments.remove(enchantment);
            if(!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantment)) continue;
            if(stack.supportsEnchantment(enchantment)) applicableEnchantments.add(enchantment);
        }
        return applicableEnchantments;
    }

    private static List<NodeBranch> buildEnchantingBranches(ItemStack stack, CostRegistry costRegistry, Canvas canvas) {
        List<NodeBranch> result = new ArrayList<>();
        List<Holder<Enchantment>> applicableEnchantments = getApplicableEnchantments(stack, costRegistry);

        List<Float> angles = generateBranchAngles(applicableEnchantments.size());

        for (int i = 0; i < applicableEnchantments.size(); i++) {
            Holder<Enchantment> enchantment = applicableEnchantments.get(i);
            int equippedLevel = stack.getEnchantmentLevel(enchantment);

            result.add(buildEnchantingBranch(enchantment, costRegistry, equippedLevel, angles.get(i), canvas));
        }
        return result;
    }

    private static NodeBranch buildEnchantingBranch(Holder<Enchantment> enchantment,
                                                    CostRegistry costRegistry,
                                                    int equippedLevel,
                                                    float angle,
                                                    Canvas canvas) {
        List<Node> nodes = new ArrayList<>();

        int maxLevel = costRegistry.get(EnchantmentUtil.toId(enchantment)).levelCosts().maxLevel();
        for (int enchantmentLevel = 0; enchantmentLevel < maxLevel; enchantmentLevel++) {
            NodeState state = equippedLevel > enchantmentLevel ? NodeState.OBTAINED : NodeState.UNOBTAINED;
            NodeTier tier = enchantmentLevel+1 == maxLevel ? NodeTier.ELITE : NodeTier.BASIC;

            if(!canvas.screen().getAvailableEnchantments().contains(enchantment)) state = NodeState.LOCKED;
            System.out.println(canvas.screen().getAvailableEnchantments());

            nodes.add(new Node(EnchantmentUtil.toId(enchantment), enchantmentLevel+1, canvas, state, tier));

            if(equippedLevel < enchantmentLevel+1) break;
        }

        return new NodeBranch(canvas, nodes, angle);
    }


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
