package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.node.*;
import me.alfie.immersiveenchanting.api.node.internal.EnchantmentNodeData;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Builds the full enchantment tree, including dependency chains and mod-filter picker delegation.
 */
public final class EnchantmentTree {

    private EnchantmentTree() {}

    /**
     * Builds the enchantment tree with dependency chains resolved first, then independent enchants.
     * Delegates to {@link FilterBranches#addPickerBranches} in hold-to-filter mode.
     */
    public static void addBranches(BuildBranchesEvent event) {
        List<Holder<Enchantment>> allEnchantments = EnchantmentUtil.getAllEnchantmentsInRegistry(
                event.getCanvas().screen().registryAccess());
        List<Holder<Enchantment>> applicableEnchantments = getApplicableEnchantments(event.getStack(), allEnchantments);

        if(event.getStack().is(ModItems.CREATIVE_BOOKSHELF_ITEM)
                || event.getStack().is(Items.BOOK)
                || event.getCanvas().screen().isState(ScreenState.BOOKS)
                || event.getStack().isEmpty()) {
            applicableEnchantments = allEnchantments;
        }

        if(event.getCanvas().screen().enchantingTab().isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            FilterBranches.addPickerBranches(event, applicableEnchantments);
            return;
        }

        var screen = event.getCanvas().screen();
        List<Holder<Enchantment>> filtered = new ArrayList<>();
        for(Holder<Enchantment> applicableEnchantment : applicableEnchantments) {
            if(!screen.matchesCurrentFilter(applicableEnchantment)) {
                continue;
            }
            if(!event.costRegistry().isRegistered(applicableEnchantment)
                    || event.costRegistry().get(applicableEnchantment).enabled()) {
                filtered.add(applicableEnchantment);
            }
        }

        Set<Holder<Enchantment>> placed = new HashSet<>();
        for(Holder<Enchantment> holder : filtered) {
            Optional<ResourceId> dependsOn = event.costRegistry().dependencies().getDependsOn(holder);
            if(dependsOn.isEmpty()) {
                buildDependencyForest(event, holder, filtered, placed, null, -1);
            }
        }
        for(Holder<Enchantment> holder : filtered) {
            if(placed.contains(holder)) continue;
            buildDependencyForest(event, holder, filtered, placed, null, -1);
        }
    }

    private static void buildDependencyForest(
            BuildBranchesEvent event,
            Holder<Enchantment> holder,
            List<Holder<Enchantment>> applicable,
            Set<Holder<Enchantment>> placed,
            @Nullable NodeBranch originBranch,
            int originNodeIndex
    ) {
        if(placed.contains(holder)) return;

        List<NodeTemplate> chainTemplates = new ArrayList<>();
        Holder<Enchantment> current = holder;

        while(current != null && !placed.contains(current)) {
            placed.add(current);
            chainTemplates.addAll(createEnchantmentNodeTemplates(event, current));

            List<Holder<Enchantment>> children = getApplicableDependents(event, current, applicable, placed);
            List<Holder<Enchantment>> singleChildren = new ArrayList<>();
            List<Holder<Enchantment>> otherChildren = new ArrayList<>();
            for(Holder<Enchantment> child : children) {
                if(enchantmentMaxLevel(event.costRegistry(), child) == 1) {
                    singleChildren.add(child);
                } else {
                    otherChildren.add(child);
                }
            }

            if(singleChildren.size() == 1 && otherChildren.isEmpty()) {
                current = singleChildren.getFirst();
                continue;
            }

            NodeBranch branch = createBranch(event, EnchantmentUtil.toId(holder), chainTemplates, originBranch, originNodeIndex);
            event.addBranch(branch);
            int tipIndex = branch.nodes().size() - 1;

            List<Holder<Enchantment>> forks = new ArrayList<>(singleChildren);
            forks.addAll(otherChildren);
            for(Holder<Enchantment> fork : forks) {
                buildDependencyForest(event, fork, applicable, placed, branch, tipIndex);
            }
            return;
        }

        if(!chainTemplates.isEmpty()) {
            event.addBranch(createBranch(event, EnchantmentUtil.toId(holder), chainTemplates, originBranch, originNodeIndex));
        }
    }

    private static NodeBranch createBranch(
            BuildBranchesEvent event,
            ResourceId branchId,
            List<NodeTemplate> templates,
            @Nullable NodeBranch originBranch,
            int originNodeIndex
    ) {
        BranchBuilder builder = BranchBuilder.of(event.getCanvas(), branchId).nodes(templates);
        if(originBranch != null && originNodeIndex >= 0) {
            builder.attachTo(originBranch, originNodeIndex);
        }
        return builder.build();
    }

    private static List<Holder<Enchantment>> getApplicableDependents(
            BuildBranchesEvent event,
            Holder<Enchantment> parent,
            List<Holder<Enchantment>> applicable,
            Set<Holder<Enchantment>> placed
    ) {
        ResourceId parentId = EnchantmentUtil.toId(parent);
        List<Holder<Enchantment>> result = new ArrayList<>();
        for(Holder<Enchantment> candidate : applicable) {
            if(placed.contains(candidate)) continue;
            Optional<ResourceId> dependsOn = event.costRegistry().dependencies().getDependsOn(candidate);
            if(dependsOn.isPresent() && dependsOn.get().equals(parentId)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private static int enchantmentMaxLevel(CostRegistry registry, Holder<Enchantment> holder) {
        if(registry.isRegistered(holder)) {
            int max = registry.get(holder).levelCosts().maxLevel();
            if(max > 0) return max;
        }
        return holder.value().getMaxLevel();
    }

    private static List<NodeTemplate> createEnchantmentNodeTemplates(
            BuildBranchesEvent event,
            Holder<Enchantment> enchantmentHolder
    ) {
        List<NodeTemplate> nodeTemplates = new ArrayList<>();

        int maxLevel = enchantmentMaxLevel(event.costRegistry(), enchantmentHolder);
        boolean dependencyMet = CostHelper.isDependencySatisfied(
                event.getStack(), enchantmentHolder, event.costRegistry(), event.getCanvas().screen().registryAccess());

        for(int enchantmentLevel = 0; enchantmentLevel < maxLevel; enchantmentLevel++) {
            int equippedLevel = EnchantmentUtil.getEnchantmentLevel(event.getStack(), enchantmentHolder);
            NodeState state = equippedLevel > enchantmentLevel ? NodeState.OBTAINED : NodeState.UNOBTAINED;
            NodeTier tier = enchantmentLevel + 1 == maxLevel ? NodeTier.ELITE : NodeTier.BASIC;

            if(!dependencyMet
                    || !event.getCanvas().screen().getMenu().isEnchantmentAvailable(enchantmentHolder)) {
                state = NodeState.LOCKED;
            }

            ResourceId enchantmentId = EnchantmentUtil.toId(enchantmentHolder);
            nodeTemplates.add(new NodeTemplate(
                    Enchantment.getFullname(enchantmentHolder, enchantmentLevel + 1),
                    enchantmentLevel,
                    state,
                    tier,
                    new SpriteIcon(EnchantmentTextureHelper.getTexture(enchantmentId)),
                    EnchantmentNodeData.create(enchantmentId, enchantmentLevel + 1)
            ));

            if(equippedLevel < enchantmentLevel + 1 && !ServerConfig.showAllEnchantmentLevels()) break;
        }

        return nodeTemplates;
    }

    private static List<Holder<Enchantment>> getApplicableEnchantments(
            ItemStack stack,
            List<Holder<Enchantment>> allEnchantments
    ) {
        List<Holder<Enchantment>> applicableEnchantments = new ArrayList<>();

        for(Holder<Enchantment> enchantment : allEnchantments) {
            Set<Holder<Enchantment>> itemEnchantments = new HashSet<>(stack.getTagEnchantments().keySet());
            itemEnchantments.remove(enchantment);
            if(!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantment)) continue;
            if(stack.supportsEnchantment(enchantment)) applicableEnchantments.add(enchantment);
        }
        return applicableEnchantments;
    }
}
