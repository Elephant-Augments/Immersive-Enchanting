package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.filter.IsolatedFilterBranch;
import me.alfie.immersiveenchanting.api.node.*;
import me.alfie.immersiveenchanting.api.node.internal.EnchantmentNodeData;
import me.alfie.immersiveenchanting.api.node.internal.FilterNodeData;
import me.alfie.immersiveenchanting.api.node.internal.ReplicateNodeData;
import me.alfie.immersiveenchanting.api.node.internal.TransmuteNodeData;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

/**
 * Functions to create branches pre-filled with nodes.
 */
public class BranchFactory {

    /**
     * Builds the full set of tree branches for the given tool stack, posts {@link BuildBranchesEvent},
     * then assigns angles (filter-picker layout or evenly spaced).
     */
    public static List<NodeBranch> buildBranches(ItemStack stack, CostRegistry costRegistry, Canvas canvas) {
        BuildBranchesEvent event = new BuildBranchesEvent(stack, costRegistry, canvas);

        addDefaultBranches(event);

        NeoForge.EVENT_BUS.post(event);

        List<NodeBranch> branches = event.getBranches();
        if(event.getCanvas().screen().enchantingTab().isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            assignFilterBranchAngles(branches);
        } else {
            List<Float> angles = generateBranchAngles(branches.size());
            for (int i = 0; i < branches.size(); i++) {
                branches.get(i).setAngle(angles.get(i));
            }
        }

        return branches;
    }

    /**
     * Adds the default branches for the current tab/item before {@link BuildBranchesEvent} listeners run.
     */
    private static void addDefaultBranches(BuildBranchesEvent event) {
        if(event.getCanvas().screen().enchantingTab().isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            buildEnchantingBranches(event);
        } else if(event.getStack().is(ModItems.ANCIENT_BOOK.get())) {
            buildAncientBookBranches(event);
        } else {
            buildEnchantingBranches(event);
        }
    }

    /**
     * Adds the ancient-book Transmute and Replicate branches.
     */
    private static void buildAncientBookBranches(BuildBranchesEvent event) {
        buildTransmuteBranch(event);
        buildReplicateBranch(event);
    }

    /**
     * In hold-to-filter mode, delegates to {@link FilterBranches#addPickerBranches}; otherwise
     * builds one enchanting branch per applicable, filter-matching enchantment.
     */
    private static void buildEnchantingBranches(BuildBranchesEvent event) {
        List<Holder<Enchantment>> allEnchantments = EnchantmentUtil.getAllEnchantmentsInRegistry(event.getCanvas().screen().registryAccess());
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
        for (Holder<Enchantment> applicableEnchantment : applicableEnchantments) {
            if (!screen.matchesCurrentFilter(applicableEnchantment)) {
                continue;
            }
            if(!event.costRegistry().isRegistered(applicableEnchantment)
                    || event.costRegistry().get(applicableEnchantment).enabled()) {
                buildEnchantingBranch(event, applicableEnchantment);
            }
        }
    }

    /**
     * @return the picker branch id {@code immersiveenchanting:mod_filter/<filterId>}.
     */
    private static ResourceId createModFilterBranchId(String modid) {
        final String modFilterStem = "mod_filter/";
        return new ResourceId(ImmersiveEnchanting.MODID, modFilterStem + modid);
    }

    /**
     * Builds a single enchantment branch with one node per level (respecting cost registry and
     * {@link ServerConfig#showAllEnchantmentLevels()}).
     */
    private static void buildEnchantingBranch(BuildBranchesEvent event, Holder<Enchantment> enchantmentHolder) {
        List<NodeTemplate> nodeTemplates = new ArrayList<>();

        int maxLevel;
        if(event.costRegistry().isRegistered(enchantmentHolder)) {
            //Use the max level from the cost registry, which may be higher than the enchantment's inherent max level if the datapack adds extra levels.
            maxLevel = event.costRegistry().get(enchantmentHolder).levelCosts().maxLevel();
        } else {
            //Use the max level from the enchantment itself as a fallback if it's not in the cost registry. This allows enchantments added by datapacks to still show up.
            maxLevel = enchantmentHolder.value().getMaxLevel();
        }

        for (int enchantmentLevel = 0; enchantmentLevel < maxLevel; enchantmentLevel++) {
            int equippedLevel = EnchantmentUtil.getEnchantmentLevel(event.getStack(), enchantmentHolder);
            NodeState state = equippedLevel > enchantmentLevel ? NodeState.OBTAINED : NodeState.UNOBTAINED;
            NodeTier tier = enchantmentLevel+1 == maxLevel ? NodeTier.ELITE : NodeTier.BASIC;

            if(!event.getCanvas().screen().getMenu().isEnchantmentAvailable(enchantmentHolder))
                state = NodeState.LOCKED;


            ResourceId enchantmentId = EnchantmentUtil.toId(enchantmentHolder);
            nodeTemplates.add(new NodeTemplate(
                    Enchantment.getFullname(enchantmentHolder, enchantmentLevel+1),
                    enchantmentLevel,
                    state,
                    tier,
                    new SpriteIcon(EnchantmentTextureHelper.getTexture(enchantmentId)),
                    EnchantmentNodeData.create(enchantmentId, enchantmentLevel+1)
            ));


            if(equippedLevel < enchantmentLevel+1 && !ServerConfig.showAllEnchantmentLevels()) break;
        }

        event.addBranch(BranchBuilder.of(event.getCanvas(), EnchantmentUtil.toId(enchantmentHolder))
                .nodes(nodeTemplates)
                .build());
    }

    /**
     * Adds the Transmute branch for an ancient book, if that cost entry is enabled.
     */
    private static void buildTransmuteBranch(BuildBranchesEvent event) {
        NodeState state = event.getCanvas().screen().getMenu().isEnchantmentAvailable(EnchantmentUtil.getStoredEnchantment(event.getStack())) ?
                NodeState.UNOBTAINED : NodeState.LOCKED;
        if(EnchantmentUtil.isReplicated(event.getStack())) state = NodeState.ALERT;

        NodeTemplate transmuteNode = new NodeTemplate(
                Component.translatable("immersiveenchanting.tooltip.title.transmute"),
                0,
                state,
                NodeTier.ADVANCED,
                new SpriteIcon(EnchantmentTextureHelper.getTexture(CostRegistry.TRANSMUTE)),
                TransmuteNodeData.create()
        );

        if(!event.costRegistry().isRegistered(CostRegistry.TRANSMUTE)
            || event.costRegistry().get(CostRegistry.TRANSMUTE).enabled())
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.TRANSMUTE)
                    .node(transmuteNode)
                    .build());

    }

    /**
     * Adds the Replicate branch for an ancient book, if that cost entry is enabled.
     */
    private static void buildReplicateBranch(BuildBranchesEvent event) {
        NodeTemplate replicateNode = new NodeTemplate(
                Component.translatable("immersiveenchanting.tooltip.title.replicate"),
                0,
                NodeState.UNOBTAINED,
                NodeTier.ADVANCED,
                new SpriteIcon(EnchantmentTextureHelper.getTexture(CostRegistry.REPLICATE)),
                ReplicateNodeData.create()
        );

        if(!event.costRegistry().isRegistered(CostRegistry.REPLICATE)
            || event.costRegistry().get(CostRegistry.REPLICATE).enabled())
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.REPLICATE)
                    .node(replicateNode)
                    .build());
    }

    /**
     * Returns all enchantments from {@code allEnchantments} that are compatible with {@code stack}
     * (i.e. supported by the item and not conflicting with its other enchantments), sorted alphabetically.
     * @param stack The item stack for which to find applicable enchantments.
     * @param allEnchantments A list of enchantments to filter, typically all enchantments in the cost registry or all enchantments in the game.
     * @return A list of enchantments from {@code allEnchantments} that are compatible with {@code stack}, sorted alphabetically.
     */
    private static List<Holder<Enchantment>> getApplicableEnchantments(ItemStack stack, List<Holder<Enchantment>> allEnchantments) {
        List<Holder<Enchantment>> applicableEnchantments = new ArrayList<>();

        for(Holder<Enchantment> enchantment : allEnchantments) {
            Set<Holder<Enchantment>> itemEnchantments = new HashSet<>(stack.getTagEnchantments().keySet());
            itemEnchantments.remove(enchantment);
            if(!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantment)) continue;
            if(stack.supportsEnchantment(enchantment)) applicableEnchantments.add(enchantment);
        }
        return applicableEnchantments;
    }

    /**
     * Places All at the top center, Unlocked to its right, and Minecraft to its left.
     * Registered isolated filter branches (e.g. Cosmetics) appear to the left of All when present.
     */
    private static void assignFilterBranchAngles(List<NodeBranch> branches) {
        if (branches.isEmpty()) {
            return;
        }

        ResourceId allId = createModFilterBranchId(FilterNodeData.ALL_MODS);
        ResourceId unlockedId = createModFilterBranchId(FilterNodeData.UNLOCKED_ONLY);
        ResourceId minecraftId = createModFilterBranchId("minecraft");
        List<NodeBranch> remaining = new ArrayList<>();
        List<NodeBranch> isolatedBranches = new ArrayList<>();
        NodeBranch allBranch = null;
        NodeBranch unlockedBranch = null;
        NodeBranch minecraftBranch = null;

        for (NodeBranch branch : branches) {
            if (branch.id().equals(allId)) {
                allBranch = branch;
            } else if (branch.id().equals(unlockedId)) {
                unlockedBranch = branch;
            } else if (branch.id().equals(minecraftId)) {
                minecraftBranch = branch;
            } else if (isIsolatedPickerBranch(branch.id())) {
                isolatedBranches.add(branch);
            } else {
                remaining.add(branch);
            }
        }

        float step = (float) (2 * Math.PI / branches.size());
        float allAngle = (float) -Math.PI / 2f;
        float unlockedAngle = allAngle + step;
        List<Float> isolatedAngles = new ArrayList<>(isolatedBranches.size());
        for (int i = 0; i < isolatedBranches.size(); i++) {
            isolatedAngles.add(allAngle - step * (i + 1));
        }
        float minecraftAngle = allAngle - step * (isolatedBranches.size() + 1);

        if (allBranch != null) {
            allBranch.setAngle(allAngle);
        }
        if (unlockedBranch != null) {
            unlockedBranch.setAngle(unlockedAngle);
        }
        for (int i = 0; i < isolatedBranches.size(); i++) {
            isolatedBranches.get(i).setAngle(isolatedAngles.get(i));
        }
        if (minecraftBranch != null) {
            minecraftBranch.setAngle(minecraftAngle);
        }

        float nextAngle = allAngle + step * 2;
        for (NodeBranch branch : remaining) {
            while (anglesNear(nextAngle, allAngle)
                    || (unlockedBranch != null && anglesNear(nextAngle, unlockedAngle))
                    || (minecraftBranch != null && anglesNear(nextAngle, minecraftAngle))
                    || isNearAny(nextAngle, isolatedAngles)) {
                nextAngle += step;
            }
            branch.setAngle(nextAngle);
            nextAngle += step;
        }
    }

    /**
     * @return {@code true} if {@code branchId} matches a registered {@link IsolatedFilterBranch} picker id.
     */
    private static boolean isIsolatedPickerBranch(ResourceId branchId) {
        for (IsolatedFilterBranch isolated : FilterBranches.isolated()) {
            if (isolated.pickerBranchId().equals(branchId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code true} if {@code angle} is nearly equal to any value in {@code angles}.
     */
    private static boolean isNearAny(float angle, List<Float> angles) {
        for (float other : angles) {
            if (anglesNear(angle, other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code true} if two angles are nearly equal on the unit circle.
     */
    private static boolean anglesNear(float a, float b) {
        float delta = Math.abs(a - b) % (float) (2 * Math.PI);
        if (delta > Math.PI) {
            delta = (float) (2 * Math.PI) - delta;
        }
        return delta < 0.0001f;
    }

    /**
     * Generates evenly-spaced angles (in radians) for {@code totalBranches} branches,
     * spread uniformly around a full circle.
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
