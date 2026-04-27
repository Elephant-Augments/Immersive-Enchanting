package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.api.enchanting_tab.BranchBuilder;
import me.alfie.immersiveenchanting.api.enchanting_tab.BuildBranchesEvent;
import me.alfie.immersiveenchanting.api.enchanting_tab.NodeTemplate;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Functions to create branches pre-filled with nodes.
 */
public class BranchFactory {

    public static List<NodeBranch> buildBranches(ItemStack stack, CostRegistry costRegistry, Canvas canvas) {
        BuildBranchesEvent event = new BuildBranchesEvent(stack, costRegistry, canvas);

        addDefaultBranches(event);

        NeoForge.EVENT_BUS.post(event);

        List<NodeBranch> branches = event.getBranches();
        List<Float> angles = generateBranchAngles(branches.size());

        for (int i = 0; i < branches.size(); i++) {
            branches.get(i).setAngle(angles.get(i));
        }

        return branches;
    }

    private static void addDefaultBranches(BuildBranchesEvent event) {
        if(event.getStack().is(ModItems.ANCIENT_BOOK.get())) {
            buildAncientBookBranches(event);
        } else {
            buildEnchantingBranches(event);
        }
    }

    private static void buildAncientBookBranches(BuildBranchesEvent event) {
        buildTransmuteBranch(event);
        buildReplicateBranch(event);

        event.addBranch(
                BranchBuilder.of(event.getCanvas(), Identifier.fromNamespaceAndPath("test", "test"))
                        .node(new NodeTemplate(
                                1,
                                NodeState.OBTAINED,
                                NodeTier.ELITE,
                                NodeType.ACTION
                        ))
                        .build());
    }

    private static void buildEnchantingBranches(BuildBranchesEvent event) {
        List<Holder<Enchantment>> applicableEnchantments = getApplicableEnchantments(event.getStack(), event.costRegistry());

        for (Holder<Enchantment> applicableEnchantment : applicableEnchantments) {
            buildEnchantingBranch(event, applicableEnchantment);
        }
    }

    private static void buildEnchantingBranch(BuildBranchesEvent event, Holder<Enchantment> enchantmentHolder) {
        List<NodeTemplate> nodeTemplates = new ArrayList<>();
        int maxLevel = event.costRegistry().get(enchantmentHolder).levelCosts().maxLevel();
        for (int enchantmentLevel = 0; enchantmentLevel < maxLevel; enchantmentLevel++) {
            int equippedLevel = event.getStack().getEnchantmentLevel(enchantmentHolder);
            NodeState state = equippedLevel > enchantmentLevel ? NodeState.OBTAINED : NodeState.UNOBTAINED;
            NodeTier tier = enchantmentLevel+1 == maxLevel ? NodeTier.ELITE : NodeTier.BASIC;

            if(!event.getCanvas().screen().getMenu().isEnchantmentAvailable(enchantmentHolder))
                state = NodeState.LOCKED;

            nodeTemplates.add(new NodeTemplate(
                    enchantmentLevel+1,
                    state,
                    tier,
                    NodeType.ENCHANTMENT));

            if(equippedLevel < enchantmentLevel+1) break;
        }

        event.addBranch(BranchBuilder.of(event.getCanvas(), EnchantmentUtil.toId(enchantmentHolder))
                .nodes(nodeTemplates)
                .build());
    }

    private static void buildTransmuteBranch(BuildBranchesEvent event) {
        NodeState state = EnchantmentUtil.isReplicated(event.getStack()) ?
                NodeState.ALERT : NodeState.UNOBTAINED;

        NodeTemplate transmuteNode = new NodeTemplate(
                1,
                state,
                NodeTier.ADVANCED,
                NodeType.ACTION
        );

        if(ServerConfig.isAllowTransmute())
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.TRANSMUTE)
                    .node(transmuteNode)
                    .build());
    }

    private static void buildReplicateBranch(BuildBranchesEvent event) {
        NodeTemplate replicateNode = new NodeTemplate(
                1,
                NodeState.UNOBTAINED,
                NodeTier.ADVANCED,
                NodeType.ACTION
        );

        if(ServerConfig.isAllowReplicate())
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.REPLICATE)
                    .node(replicateNode)
                    .build());
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
