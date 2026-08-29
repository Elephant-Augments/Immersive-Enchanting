package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.filter.IsolatedFilterBranch;
import me.alfie.immersiveenchanting.api.node.internal.FilterNodeData;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Angular layout, spacing metrics, and topological placement for branch trees.
 */
public final class BranchLayout {

    public static final int MAX_NODE_STEP = 120;
    private static final int MIN_NODE_STEP = 40;

    private BranchLayout() {}

    /**
     * Assigns branch angles at the root, then fans dependency child nodes.
     */
    public static void assignAngles(List<NodeBranch> branches, EnchantingTab.Display display) {
        List<NodeBranch> rootBranches = branches.stream().filter(branch -> !branch.hasOrigin()).toList();
        if(display == EnchantingTab.Display.MOD_FILTERS) {
            assignFilterBranchAngles(rootBranches);
        } else {
            List<Float> angles = generateBranchAngles(rootBranches.size());
            for(int i = 0; i < rootBranches.size(); i++) {
                rootBranches.get(i).setAngle(angles.get(i));
            }
        }
        assignAttachedBranchAngles(branches);
    }

    /**
     * Computes node step and scale from the smallest angular gap between root trunks.
     */
    public static BranchSpacing computeSpacing(List<NodeBranch> branches) {
        final int baseStep = MIN_NODE_STEP;
        final int minStep = MIN_NODE_STEP;
        final int maxStep = MAX_NODE_STEP;
        final float minScale = 0.3f;
        final float maxScale = 1f;
        final int margin = 4;
        final float nodeSize = Math.max(Node.WIDTH + margin, Node.HEIGHT + margin) * Node.DEFAULT_SCALE;

        List<NodeBranch> spacingBranches = branches.stream()
                .filter(branch -> !branch.hasOrigin())
                .toList();

        if(spacingBranches.size() <= 1) {
            return new BranchSpacing(baseStep, Math.min(maxScale, Node.DEFAULT_SCALE));
        }

        List<NodeBranch> sorted = new ArrayList<>(spacingBranches);
        sorted.sort(Comparator.comparingDouble(NodeBranch::angle));

        double smallestAngle = Double.MAX_VALUE;
        for(int i = 0; i < sorted.size(); i++) {
            double a1 = sorted.get(i).angle();
            double a2 = sorted.get((i + 1) % sorted.size()).angle();
            double difference = Math.abs(a2 - a1);
            difference = Math.min(difference, 2 * Math.PI - difference);
            smallestAngle = Math.min(smallestAngle, difference);
        }

        int nodeStep = baseStep;
        double minDistance = nodeSize;
        double currentDistance = nodeStep * smallestAngle;
        float safety = 1.1f;

        if(currentDistance < minDistance * safety) {
            nodeStep = (int) Math.ceil((minDistance * safety) / smallestAngle);
        }
        nodeStep = Math.max(minStep, Math.min(nodeStep, maxStep));

        float scale = Node.DEFAULT_SCALE;
        currentDistance = nodeStep * smallestAngle;
        if(currentDistance < minDistance) {
            scale = (float) (currentDistance / minDistance);
            scale = Math.max(scale, minScale);
        }
        scale = Math.min(scale, maxScale);

        return new BranchSpacing(nodeStep, scale);
    }

    /**
     * Positions every branch in parent-before-child order using {@code spacing}.
     */
    public static void positionAll(List<NodeBranch> branches, BranchSpacing spacing) {
        List<NodeBranch> remaining = new ArrayList<>(branches);
        while(!remaining.isEmpty()) {
            boolean progress = false;
            for(var iterator = remaining.iterator(); iterator.hasNext(); ) {
                NodeBranch branch = iterator.next();
                if(branch.hasOrigin() && remaining.contains(branch.originBranch())) {
                    continue;
                }
                branch.placeNodesAlongLine(spacing);
                iterator.remove();
                progress = true;
            }
            if(!progress) {
                for(NodeBranch branch : remaining) {
                    branch.placeNodesAlongLine(spacing);
                }
                break;
            }
        }
    }

    private static void assignAttachedBranchAngles(List<NodeBranch> branches) {
        Map<NodeBranch, List<NodeBranch>> childrenByOrigin = new HashMap<>();
        for(NodeBranch branch : branches) {
            if(!branch.hasOrigin()) continue;
            childrenByOrigin.computeIfAbsent(branch.originBranch(), ignored -> new ArrayList<>()).add(branch);
        }

        boolean progress = true;
        Set<NodeBranch> assigned = new HashSet<>();
        for(NodeBranch branch : branches) {
            if(!branch.hasOrigin()) assigned.add(branch);
        }

        while(progress) {
            progress = false;
            for(Map.Entry<NodeBranch, List<NodeBranch>> entry : childrenByOrigin.entrySet()) {
                NodeBranch parent = entry.getKey();
                if(parent == null || !assigned.contains(parent)) continue;

                List<NodeBranch> children = new ArrayList<>(entry.getValue());
                children.sort(Comparator.comparing(branch -> branch.id().toString()));
                assignSiblingFanAngles(parent, children);

                for(NodeBranch child : children) {
                    if(assigned.contains(child)) continue;
                    assigned.add(child);
                    progress = true;
                }
            }
        }

        for(NodeBranch branch : branches) {
            if(!assigned.contains(branch) && branch.hasOrigin() && branch.originBranch() != null) {
                branch.setAngle(branch.originBranch().angle());
            }
        }
    }

    private static void assignSiblingFanAngles(NodeBranch parent, List<NodeBranch> children) {
        if(children.isEmpty()) {
            return;
        }
        if(children.size() == 1) {
            children.getFirst().setAngle(parent.angle());
            return;
        }

        float maxSpan = (float) (Math.PI / 4);
        float minGap = (float) (Math.PI / 18);
        float gap = Math.max(minGap, maxSpan / (children.size() - 1));
        gap = Math.min(gap, (float) (Math.PI / 12));

        float totalSpan = gap * (children.size() - 1);
        float start = parent.angle() - totalSpan / 2f;
        for(int i = 0; i < children.size(); i++) {
            children.get(i).setAngle(start + gap * i);
        }
    }

    private static void assignFilterBranchAngles(List<NodeBranch> branches) {
        if(branches.isEmpty()) {
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

        for(NodeBranch branch : branches) {
            if(branch.id().equals(allId)) {
                allBranch = branch;
            } else if(branch.id().equals(unlockedId)) {
                unlockedBranch = branch;
            } else if(branch.id().equals(minecraftId)) {
                minecraftBranch = branch;
            } else if(isIsolatedPickerBranch(branch.id())) {
                isolatedBranches.add(branch);
            } else {
                remaining.add(branch);
            }
        }

        float step = (float) (2 * Math.PI / branches.size());
        float allAngle = (float) -Math.PI / 2f;
        float unlockedAngle = allAngle + step;
        List<Float> isolatedAngles = new ArrayList<>(isolatedBranches.size());
        for(int i = 0; i < isolatedBranches.size(); i++) {
            isolatedAngles.add(allAngle - step * (i + 1));
        }
        float minecraftAngle = allAngle - step * (isolatedBranches.size() + 1);

        if(allBranch != null) {
            allBranch.setAngle(allAngle);
        }
        if(unlockedBranch != null) {
            unlockedBranch.setAngle(unlockedAngle);
        }
        for(int i = 0; i < isolatedBranches.size(); i++) {
            isolatedBranches.get(i).setAngle(isolatedAngles.get(i));
        }
        if(minecraftBranch != null) {
            minecraftBranch.setAngle(minecraftAngle);
        }

        float nextAngle = allAngle + step * 2;
        for(NodeBranch branch : remaining) {
            while(anglesNear(nextAngle, allAngle)
                    || (unlockedBranch != null && anglesNear(nextAngle, unlockedAngle))
                    || (minecraftBranch != null && anglesNear(nextAngle, minecraftAngle))
                    || isNearAny(nextAngle, isolatedAngles)) {
                nextAngle += step;
            }
            branch.setAngle(nextAngle);
            nextAngle += step;
        }
    }

    private static ResourceId createModFilterBranchId(String modid) {
        final String modFilterStem = "mod_filter/";
        return new ResourceId(ImmersiveEnchanting.MODID, modFilterStem + modid);
    }

    private static boolean isIsolatedPickerBranch(ResourceId branchId) {
        for(IsolatedFilterBranch isolated : FilterBranches.isolated()) {
            if(isolated.pickerBranchId().equals(branchId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNearAny(float angle, List<Float> angles) {
        for(float other : angles) {
            if(anglesNear(angle, other)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anglesNear(float a, float b) {
        float delta = Math.abs(a - b) % (float) (2 * Math.PI);
        if(delta > Math.PI) {
            delta = (float) (2 * Math.PI) - delta;
        }
        return delta < 0.0001f;
    }

    private static ArrayList<Float> generateBranchAngles(int totalBranches) {
        ArrayList<Float> angles = new ArrayList<>();
        for(int i = 0; i < totalBranches; i++) {
            float angle = (float) (i * 2 * Math.PI / totalBranches);
            angles.add(angle);
        }
        return angles;
    }
}
