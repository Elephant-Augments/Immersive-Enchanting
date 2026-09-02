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
 *
 * <p>Layout runs in two angle passes around spacing:</p>
 * <ol>
 *   <li>{@link #assignAngles} — initial root strategy + child fans</li>
 *   <li>{@link #computeSpacing} then {@link #refineStandaloneRootAngles} — ring scale + clearance</li>
 *   <li>{@link #positionAll} — place nodes along each branch</li>
 * </ol>
 */
public final class BranchLayout {

    public static final int MAX_NODE_STEP = 120;
    public static final int MIN_NODE_STEP = 60;
    private static final float MIN_BRANCH_SCALE = 0.68f;
    public static final float STANDALONE_ROOT_INNER_RADIUS_SCALE = 0.72f;
    private static final float STANDALONE_ROOT_OUTER_RADIUS_SCALE = 1.34f;
    private static final float CHILD_FIRST_SEGMENT_SCALE = 0.9f;
    private static final int BUSY_CHAIN_COUNT_THRESHOLD = 8;
    private static final float BUSY_CHAIN_STEP_THRESHOLD = (float) (Math.PI / 10);
    private static final float BUSY_ANGULAR_GAP_THRESHOLD = (float) (Math.PI / 9);
    private static final float SPACING_SAFETY = 1.12f;
    private static final float BUSY_SPACING_SAFETY = 1.24f;
    private static final int NODE_CLEARANCE_MARGIN = 6;
    private static final float BUSY_LINE_CLEARANCE = 8f;
    private static final float STANDALONE_CLEARANCE_MARGIN = 6f;
    private static final int STANDALONE_ANGLE_SAMPLES = 16;

    private BranchLayout() {}

    /**
     * Resolved placement for standalone roots after spacing is known.
     *
     * @param radiusScale radial distance multiplier applied to the trunk step
     */
    public record StandaloneRootPlacement(float radiusScale) {}

    public static float childFirstSegmentScale() {
        return CHILD_FIRST_SEGMENT_SCALE;
    }

    /**
     * A root branch with multiple chaining nodes or attached child branches.
     */
    static boolean isChainRoot(NodeBranch branch, List<NodeBranch> allBranches) {
        if(branch.nodes().size() > 1) {
            return true;
        }
        for(NodeBranch other : allBranches) {
            if(other.hasOrigin() && other.originBranch() == branch) {
                return true;
            }
        }
        return false;
    }

    /**
     * A root branch with exactly one node and no attached child branches.
     */
    public static boolean isStandaloneRoot(NodeBranch branch, List<NodeBranch> allBranches) {
        return !branch.hasOrigin() && !isChainRoot(branch, allBranches);
    }

    /**
     * Computes continuation steps and branch scale from the angular gaps between root nodes.
     */
    public static BranchSpacing computeSpacing(List<NodeBranch> branches) {
        final int baseStep = MIN_NODE_STEP;
        final int minStep = MIN_NODE_STEP;
        final int maxStep = MAX_NODE_STEP;
        final float minScale = MIN_BRANCH_SCALE;
        final float maxScale = 1f;

        List<NodeBranch> spacingBranches = branches.stream()
                .filter(branch -> !branch.hasOrigin())
                .toList();

        if(spacingBranches.size() <= 1) {
            return new BranchSpacing(
                    baseStep,
                    baseStep,
                    Math.min(maxScale, Node.DEFAULT_SCALE)
            );
        }

        double chainGap = smallestChainToChainGap(spacingBranches, branches);
        double adjacentGap = smallestAdjacentAngularGap(spacingBranches);
        double smallestAngle = Double.isInfinite(chainGap) ? adjacentGap : chainGap;
        if(!Double.isInfinite(chainGap) && hasStandaloneRoots(spacingBranches, branches)) {
            smallestAngle = Math.min(smallestAngle, adjacentGap);
        }

        boolean busyTree = isBusySpacingLayout(spacingBranches.size(), smallestAngle);
        float safety = busyTree ? BUSY_SPACING_SAFETY : SPACING_SAFETY;
        float nodeSize = Math.max(Node.WIDTH + NODE_CLEARANCE_MARGIN, Node.HEIGHT + NODE_CLEARANCE_MARGIN)
                * Node.DEFAULT_SCALE;
        if(busyTree) {
            nodeSize += BUSY_LINE_CLEARANCE;
        }

        int continuationStep = baseStep;
        double minDistance = nodeSize;
        double currentDistance = continuationStep * smallestAngle;

        if(currentDistance < minDistance * safety) {
            continuationStep = (int) Math.ceil((minDistance * safety) / smallestAngle);
        }
        continuationStep = Math.max(minStep, Math.min(continuationStep, maxStep));

        int trunkStep = Math.max(MIN_NODE_STEP, continuationStep);

        float scale = Node.DEFAULT_SCALE;
        currentDistance = continuationStep * smallestAngle;
        if(currentDistance < minDistance) {
            scale = (float) (currentDistance / minDistance);
            scale = Math.max(scale, minScale);
        }
        scale = Math.min(scale, maxScale);

        return new BranchSpacing(trunkStep, continuationStep, scale);
    }

    /**
     * Assigns angles to branches depending on the current display mode,
     * starting with the root nodes before fanning out child nodes.
     */
    public static void assignAngles(List<NodeBranch> branches, EnchantingTab.Display display) {
        List<NodeBranch> rootBranches = branches.stream().filter(branch -> !branch.hasOrigin()).toList();
        if(display == EnchantingTab.Display.MOD_FILTERS) {
            assignRootAnglesForFilters(rootBranches);
        } else {
            assignRootAngles(rootBranches, branches);
        }
        assignChildBranchAngles(branches, rootBranches);
    }

    /**
     * Places chain roots evenly around the circle and seeds standalone roots into the gaps.
     * Falls back to even spacing when the tree is chains-only or standalones-only.
     */
    private static void assignRootAngles(List<NodeBranch> rootBranches, List<NodeBranch> allBranches) {
        if(rootBranches.isEmpty()) {
            return;
        }

        List<NodeBranch> chains = new ArrayList<>();
        List<NodeBranch> standalones = new ArrayList<>();
        for(NodeBranch branch : rootBranches) {
            if(isChainRoot(branch, allBranches)) {
                chains.add(branch);
            } else {
                standalones.add(branch);
            }
        }

        chains.sort(Comparator.comparing(branch -> branch.id().toString()));
        standalones.sort(Comparator.comparing(branch -> branch.id().toString()));

        if(chains.isEmpty() || standalones.isEmpty()) {
            assignRootAnglesEvenly(rootBranches);
            return;
        }

        float chainStep = (float) (2 * Math.PI / chains.size());
        for(int i = 0; i < chains.size(); i++) {
            chains.get(i).setAngle(i * chainStep);
        }

        float standaloneOffsetStep = (float) (Math.PI / 36);
        for(int i = 0; i < standalones.size(); i++) {
            int gapIndex = i % chains.size();
            int layer = i / chains.size();
            float midpoint = (gapIndex + 0.5f) * chainStep;
            float offset = (layer + 1) / 2f * standaloneOffsetStep * (layer % 2 == 0 ? 1f : -1f);
            standalones.get(i).setAngle(normalizeAngle(midpoint + offset));
        }
    }

    /** Evenly spaces every root around the full circle. */
    private static void assignRootAnglesEvenly(List<NodeBranch> rootBranches) {
        List<Float> angles = generateBranchAngles(rootBranches.size());
        for(int i = 0; i < rootBranches.size(); i++) {
            rootBranches.get(i).setAngle(angles.get(i));
        }
    }

    /**
     * Refines standalone-root angles after placement of neighboring chain roots.
     * Standalone nodes are placed farther out on denser trees to avoid overlap.
     */
    public static StandaloneRootPlacement refineStandaloneRootAngles(List<NodeBranch> branches, BranchSpacing spacing) {
        List<NodeBranch> rootBranches = branches.stream().filter(branch -> !branch.hasOrigin()).toList();
        List<NodeBranch> chains = partitionChainRoots(rootBranches, branches);
        List<NodeBranch> standalones = partitionStandaloneRoots(rootBranches, branches);

        if(chains.isEmpty() || standalones.isEmpty()) {
            return new StandaloneRootPlacement(STANDALONE_ROOT_INNER_RADIUS_SCALE);
        }

        chains.sort(Comparator.comparingDouble(NodeBranch::angle));
        standalones.sort(Comparator.comparing(branch -> branch.id().toString()));

        float chainStep = (float) (2 * Math.PI / chains.size());
        boolean busyLayout = isBusyStandaloneLayout(chains.size(), chainStep);
        float standaloneRadiusScale = busyLayout
                ? STANDALONE_ROOT_OUTER_RADIUS_SCALE
                : STANDALONE_ROOT_INNER_RADIUS_SCALE;

        float chainRadius = spacing.trunkStep();
        float standaloneRadius = spacing.trunkStep() * standaloneRadiusScale;
        float nodeRadius = scaledNodeRadius(spacing.branchScale()) + STANDALONE_CLEARANCE_MARGIN;

        int centerX = chains.getFirst().canvas().getCenter().x();
        int centerY = chains.getFirst().canvas().getCenter().y();

        for(int i = 0; i < standalones.size(); i++) {
            NodeBranch standalone = standalones.get(i);
            int gapIndex = i % chains.size();
            float gapStart = chains.get(gapIndex).angle();
            float gapEnd = chains.get((gapIndex + 1) % chains.size()).angle();
            if(gapEnd <= gapStart) {
                gapEnd += (float) (2 * Math.PI);
            }

            float bestAngle = standalone.angle();
            float bestClearance = -1f;
            for(int sample = 1; sample < STANDALONE_ANGLE_SAMPLES; sample++) {
                float t = (float) sample / STANDALONE_ANGLE_SAMPLES;
                float angle = gapStart + (gapEnd - gapStart) * t;
                float clearance = minClearanceToChains(
                        centerX, centerY, angle, standaloneRadius,
                        chains, centerX, centerY, chainRadius
                );
                if(clearance > bestClearance) {
                    bestClearance = clearance;
                    bestAngle = angle;
                }
            }

            if(bestClearance >= nodeRadius * 2f) {
                standalone.setAngle(normalizeAngle(bestAngle));
            }
        }

        return new StandaloneRootPlacement(standaloneRadiusScale);
    }

    private static float minClearanceToChains(
            int standaloneCenterX,
            int standaloneCenterY,
            float standaloneAngle,
            float standaloneRadius,
            List<NodeBranch> chains,
            int chainCenterX,
            int chainCenterY,
            float chainRadius
    ) {
        float standaloneX = standaloneCenterX + (float) Math.cos(standaloneAngle) * standaloneRadius;
        float standaloneY = standaloneCenterY + (float) Math.sin(standaloneAngle) * standaloneRadius;

        float minCenterDistance = Float.MAX_VALUE;
        for(NodeBranch chain : chains) {
            float chainX = chainCenterX + (float) Math.cos(chain.angle()) * chainRadius;
            float chainY = chainCenterY + (float) Math.sin(chain.angle()) * chainRadius;
            float dx = standaloneX - chainX;
            float dy = standaloneY - chainY;
            minCenterDistance = Math.min(minCenterDistance, (float) Math.hypot(dx, dy));
        }
        return minCenterDistance;
    }

    private static float scaledNodeRadius(float branchScale) {
        return Math.max(Node.WIDTH, Node.HEIGHT) * Node.DEFAULT_SCALE * branchScale / 2f;
    }

    private static boolean isBusyStandaloneLayout(int chainCount, float chainStep) {
        return chainCount >= BUSY_CHAIN_COUNT_THRESHOLD
                || chainStep < BUSY_CHAIN_STEP_THRESHOLD;
    }

    private static boolean isBusySpacingLayout(int rootCount, double smallestAngle) {
        return rootCount >= BUSY_CHAIN_COUNT_THRESHOLD
                || smallestAngle < BUSY_ANGULAR_GAP_THRESHOLD;
    }

    private static boolean hasStandaloneRoots(List<NodeBranch> rootBranches, List<NodeBranch> allBranches) {
        for(NodeBranch branch : rootBranches) {
            if(isStandaloneRoot(branch, allBranches)) {
                return true;
            }
        }
        return false;
    }

    private static List<NodeBranch> partitionChainRoots(List<NodeBranch> rootBranches, List<NodeBranch> allBranches) {
        List<NodeBranch> chains = new ArrayList<>();
        for(NodeBranch branch : rootBranches) {
            if(isChainRoot(branch, allBranches)) {
                chains.add(branch);
            }
        }
        return chains;
    }

    private static List<NodeBranch> partitionStandaloneRoots(List<NodeBranch> rootBranches, List<NodeBranch> allBranches) {
        List<NodeBranch> standalones = new ArrayList<>();
        for(NodeBranch branch : rootBranches) {
            if(isStandaloneRoot(branch, allBranches)) {
                standalones.add(branch);
            }
        }
        return standalones;
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

    private static void assignChildBranchAngles(List<NodeBranch> branches, List<NodeBranch> rootBranches) {
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
                assignSiblingFanAngles(parent, children, rootBranches);

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

    private static void assignSiblingFanAngles(
            NodeBranch parent,
            List<NodeBranch> children,
            List<NodeBranch> rootBranches
    ) {
        if(children.isEmpty()) {
            return;
        }
        if(children.size() == 1) {
            children.getFirst().setAngle(parent.angle());
            return;
        }

        float maxSpan = maxAllowedSiblingSpan(parent, rootBranches);
        float minGap = (float) (Math.PI / 18);
        float gap = Math.max(minGap, maxSpan / (children.size() - 1));
        gap = Math.min(gap, (float) (Math.PI / 12));

        float totalSpan = Math.min(maxSpan, gap * (children.size() - 1));
        gap = totalSpan / (children.size() - 1);
        float start = parent.angle() - totalSpan / 2f;
        for(int i = 0; i < children.size(); i++) {
            children.get(i).setAngle(start + gap * i);
        }
    }

    /**
     * Limits sibling fan width so dependency forks stay inside their angular wedge.
     */
    private static float maxAllowedSiblingSpan(NodeBranch parent, List<NodeBranch> rootBranches) {
        float defaultSpan = (float) (Math.PI / 4);
        if(rootBranches.size() <= 1) {
            return defaultSpan;
        }

        NodeBranch root = rootTrunk(parent);
        List<NodeBranch> sorted = new ArrayList<>(rootBranches);
        sorted.sort(Comparator.comparingDouble(NodeBranch::angle));

        int index = sorted.indexOf(root);
        if(index < 0) {
            return defaultSpan;
        }

        float rootAngle = sorted.get(index).angle();
        float prevAngle = sorted.get((index - 1 + sorted.size()) % sorted.size()).angle();
        float nextAngle = sorted.get((index + 1) % sorted.size()).angle();

        float leftHalf = (float) (angularDifference(prevAngle, rootAngle) / 2.0);
        float rightHalf = (float) (angularDifference(rootAngle, nextAngle) / 2.0);
        float margin = 0.82f;
        float availableHalf = Math.min(leftHalf, rightHalf) * margin;

        float minSpan = (float) (Math.PI / 18);
        return Math.max(minSpan, Math.min(defaultSpan, availableHalf * 2f));
    }

    private static NodeBranch rootTrunk(NodeBranch branch) {
        NodeBranch current = branch;
        while(current.hasOrigin()) {
            current = current.originBranch();
        }
        return current;
    }

    private static float normalizeAngle(float angle) {
        float twoPi = (float) (2 * Math.PI);
        angle %= twoPi;
        if(angle < 0) {
            angle += twoPi;
        }
        return angle;
    }

    private static void assignRootAnglesForFilters(List<NodeBranch> branches) {
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

    private static double smallestAdjacentAngularGap(List<NodeBranch> branches) {
        List<NodeBranch> sorted = new ArrayList<>(branches);
        sorted.sort(Comparator.comparingDouble(NodeBranch::angle));

        double smallestAngle = Double.MAX_VALUE;
        for(int i = 0; i < sorted.size(); i++) {
            double difference = angularDifference(sorted.get(i).angle(), sorted.get((i + 1) % sorted.size()).angle());
            smallestAngle = Math.min(smallestAngle, difference);
        }
        return smallestAngle;
    }

    /**
     * Smallest gap between consecutive chain roots.
     * Returns {@link Double#POSITIVE_INFINITY} when fewer than two chains exist.
     */
    private static double smallestChainToChainGap(List<NodeBranch> rootBranches, List<NodeBranch> allBranches) {
        List<NodeBranch> chains = new ArrayList<>();
        for(NodeBranch branch : rootBranches) {
            if(isChainRoot(branch, allBranches)) {
                chains.add(branch);
            }
        }

        if(chains.size() <= 1) {
            return Double.POSITIVE_INFINITY;
        }

        boolean hasStandalone = false;
        for(NodeBranch branch : rootBranches) {
            if(!isChainRoot(branch, allBranches)) {
                hasStandalone = true;
                break;
            }
        }
        if(!hasStandalone) {
            return Double.POSITIVE_INFINITY;
        }

        chains.sort(Comparator.comparingDouble(NodeBranch::angle));

        double smallestAngle = Double.MAX_VALUE;
        for(int i = 0; i < chains.size(); i++) {
            double difference = angularDifference(chains.get(i).angle(), chains.get((i + 1) % chains.size()).angle());
            smallestAngle = Math.min(smallestAngle, difference);
        }
        return smallestAngle;
    }

    private static double angularDifference(double a1, double a2) {
        double difference = Math.abs(a2 - a1);
        return Math.min(difference, 2 * Math.PI - difference);
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
