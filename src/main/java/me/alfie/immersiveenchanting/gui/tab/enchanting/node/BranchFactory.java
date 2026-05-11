package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.api.node.*;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.api.node.internal.EnchantmentNodeData;
import me.alfie.immersiveenchanting.api.node.internal.ModFilterNodeData;
import me.alfie.immersiveenchanting.api.node.internal.ReplicateNodeData;
import me.alfie.immersiveenchanting.api.node.internal.TransmuteNodeData;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

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
    }

    private static void buildEnchantingBranches(BuildBranchesEvent event) {
        List<Holder<Enchantment>> applicableEnchantments = getApplicableEnchantments(event.getStack(), event.costRegistry());
        if(event.getStack().is(ModItems.CREATIVE_BOOKSHELF_ITEM)) {
            applicableEnchantments = EnchantmentUtil.sortByName(event.costRegistry().getAllEnchantmentHolders());
        }

        if(event.getCanvas().screen().enchantingTab().isDisplay(EnchantingTab.Display.ENCHANTMENTS)) {

            for (Holder<Enchantment> applicableEnchantment : applicableEnchantments) {
                String modid = applicableEnchantment.getKey().identifier().getNamespace();
                String filteredModid = event.getCanvas().screen().enchantingTab().getFilteredModid();
                if(filteredModid == null || filteredModid.equals(modid)) {
                    buildEnchantingBranch(event, applicableEnchantment);
                }
            }

        } else if(event.getCanvas().screen().enchantingTab().isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            buildModFilterBranches(event, applicableEnchantments);
        }

    }

    private static Identifier createModFilterBranchId(String modid) {
        final String modFilterStem = "mod_filter/";
        return Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, modFilterStem + modFilterStem);
    }

    private static void buildModFilterBranches(BuildBranchesEvent event, List<Holder<Enchantment>> applicableEnchantments) {
        Set<String> modids = new HashSet<>();

        for(Holder<Enchantment> enchantmentHolder : applicableEnchantments) {
            String modid = enchantmentHolder.getKey().identifier().getNamespace();
            if(modids.contains(modid)) continue;
            modids.add(modid);


            Component title = Component.literal(ImmersiveEnchanting.getModName(modid));
            buildModFilterBranch(event, title, modid);
        }

        ItemIcon itemIcon = new ItemIcon(ClientDatapackManager.get(DatapackKeys.MOD_ICONS)
                .getAsItemStack(ModFilterNodeData.ALL_MODS));

        NodeState state =
                Objects.equals(event.getCanvas().screen().enchantingTab().getFilteredModid(), null)
                        ? NodeState.OBTAINED
                        : NodeState.UNOBTAINED;

        event.addBranch(BranchBuilder.of(event.getCanvas(), createModFilterBranchId(ModFilterNodeData.ALL_MODS))
                .node(new NodeTemplate(
                        Component.translatable("immersiveenchanting.mod_filter.all"),
                        1,
                        state,
                        NodeTier.ELITE,
                        itemIcon,
                        ModFilterNodeData.create(ModFilterNodeData.ALL_MODS)
                )).build());
    }



    private static void buildModFilterBranch(BuildBranchesEvent event, Component modTitle, String modid) {
        ItemIcon itemIcon = new ItemIcon(ClientDatapackManager.get(DatapackKeys.MOD_ICONS)
                .getAsItemStack(modid));

        NodeState state =
                Objects.equals(event.getCanvas().screen().enchantingTab().getFilteredModid(), modid)
                        ? NodeState.OBTAINED
                        : NodeState.UNOBTAINED;

        event.addBranch(BranchBuilder.of(event.getCanvas(), createModFilterBranchId(modid))
                .node(new NodeTemplate(
                        modTitle,
                        1,
                        state,
                        NodeTier.BASIC,
                        itemIcon,
                        ModFilterNodeData.create(modid)
                )).build());
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

            Identifier enchantmentId = enchantmentHolder.getKey().identifier();
            nodeTemplates.add(new NodeTemplate(
                    Enchantment.getFullname(enchantmentHolder, enchantmentLevel+1),
                    enchantmentLevel+1,
                    state,
                    tier,
                    new SpriteIcon(EnchantmentTextureHelper.getTexture(enchantmentId)),
                    EnchantmentNodeData.create(enchantmentId, enchantmentLevel+1)
            ));


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
                Component.translatable("immersiveenchanting.tooltip.title.transmute"),
                1,
                state,
                NodeTier.ADVANCED,
                new SpriteIcon(EnchantmentTextureHelper.getTexture(CostRegistry.TRANSMUTE)),
                TransmuteNodeData.create()
        );

        if(ServerConfig.isAllowTransmute())
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.TRANSMUTE)
                    .node(transmuteNode)
                    .build());
    }

    private static void buildReplicateBranch(BuildBranchesEvent event) {
        NodeTemplate replicateNode = new NodeTemplate(
                Component.translatable("immersiveenchanting.tooltip.title.replicate"),
                1,
                NodeState.UNOBTAINED,
                NodeTier.ADVANCED,
                new SpriteIcon(EnchantmentTextureHelper.getTexture(CostRegistry.REPLICATE)),
                ReplicateNodeData.create()
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
