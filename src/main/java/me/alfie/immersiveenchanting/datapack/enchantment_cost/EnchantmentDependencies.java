package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds a dependency tree from valid datapack dependency entries ({@code depends_on}).
 *
 * <p>Rules:
 * <ul>
 *   <li>Dependency targets must exist and resolve to a registered enchantment.</li>
 *   <li>The parent enchantment must be single-level ({@code maxLevel == 1}).</li>
 *   <li>Multi-level enchantments may depend on a single-level parent,
 *       but can't be depended on themselves.</li>
 *   <li>Self-dependencies and cycles are rejected.</li>
 *   <li>Parent and child must share an equipment slot.</li>
 * </ul>
 * Invalid entries fallback to normal, independent branches.
 */
public final class EnchantmentDependencies {

    private final Map<ResourceId, ResourceId> effectiveDependsOn = new HashMap<>();
    private final Map<ResourceId, List<ResourceId>> effectiveChildren = new HashMap<>();
    private final List<String> brokenReports = new ArrayList<>();

    public void clear() {
        effectiveDependsOn.clear();
        effectiveChildren.clear();
        brokenReports.clear();
    }

    public List<String> brokenReports() {
        return List.copyOf(brokenReports);
    }

    public Optional<ResourceId> getDependsOn(ResourceId id) {
        return Optional.ofNullable(effectiveDependsOn.get(id));
    }

    public Optional<ResourceId> getDependsOn(Holder<Enchantment> holder) {
        return getDependsOn(ResourceId.parse(holder.getRegisteredName()));
    }

    public List<ResourceId> getChildren(ResourceId parentId) {
        return effectiveChildren.getOrDefault(parentId, List.of());
    }

    public void rebuild(CostRegistry registry) {
        clear();

        Map<ResourceId, ResourceId> tentative = new HashMap<>();

        for(ResourceId childId : registry.getAllEnchantmentIds()) {
            CostData data = registry.get(childId);
            Optional<ResourceLocation> raw = data.dependsOn();
            if(raw.isEmpty()) continue;

            ResourceId parentId = ResourceId.parse(raw.get().toString());
            String childName = childId.toString();

            if(parentId.equals(childId)) {
                brokenReports.add(childName + " (depends on itself)");
                continue;
            }
            if(!registry.isRegistered(parentId)) {
                brokenReports.add(childName + " (missing dependency " + parentId + ")");
                continue;
            }

            int parentMax = maxLevel(registry, parentId);
            if(parentMax != 1) {
                brokenReports.add(childName + " (dependency " + parentId + " is not single-level)");
                continue;
            }

            Optional<Holder<Enchantment>> childHolder = findHolder(registry, childId);
            Optional<Holder<Enchantment>> parentHolder = findHolder(registry, parentId);
            if(childHolder.isPresent() && parentHolder.isPresent()
                    && !slotsCompatible(childHolder.get(), parentHolder.get())) {
                brokenReports.add(formatCrossSlotReport(childHolder.get(), parentHolder.get()));
                continue;
            }

            tentative.put(childId, parentId);
        }

        // Multi-level enchantments can't be mid-chain parents.
        Set<ResourceId> midChainParents = new HashSet<>(tentative.values());
        List<ResourceId> invalidChildren = new ArrayList<>();
        for(ResourceId childId : tentative.keySet()) {
            if(midChainParents.contains(childId) && maxLevel(registry, childId) != 1) {
                brokenReports.add(childId + " (multi-level enchantment used as a dependency parent)");
                invalidChildren.add(childId);
            }
        }
        // Also invalidate entries whose parent was an invalid mid-chain multi-level
        for(Map.Entry<ResourceId, ResourceId> entry : List.copyOf(tentative.entrySet())) {
            if(invalidChildren.contains(entry.getValue())) {
                brokenReports.add(entry.getKey() + " (dependency " + entry.getValue() + " is invalid)");
                tentative.remove(entry.getKey());
            }
        }
        for(ResourceId id : invalidChildren) {
            tentative.remove(id);
        }

        // Remove every entry that participates in a cycle.
        Set<ResourceId> cyclic = findCyclicNodes(tentative);
        if(!cyclic.isEmpty()) {
            for(ResourceId id : List.copyOf(tentative.keySet())) {
                if(cyclic.contains(id) || cyclic.contains(tentative.get(id))) {
                    brokenReports.add(id + " (dependency cycle involving " + tentative.get(id) + ")");
                    tentative.remove(id);
                }
            }
        }

        for(Map.Entry<ResourceId, ResourceId> entry : tentative.entrySet()) {
            effectiveDependsOn.put(entry.getKey(), entry.getValue());
            effectiveChildren
                    .computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>())
                    .add(entry.getKey());
        }

        if(!brokenReports.isEmpty()) {
            ImmersiveEnchanting.LOGGER.error("Broken enchantment dependencies (ignored for layout): {}", brokenReports);
        }
    }

    private static int maxLevel(CostRegistry registry, ResourceId id) {
        CostData data = registry.get(id);
        int fromCosts = data.levelCosts().maxLevel();
        if(fromCosts > 0) return fromCosts;
        return 1;
    }

    private static Optional<Holder<Enchantment>> findHolder(CostRegistry registry, ResourceId id) {
        for(Holder<Enchantment> holder : registry.getAllEnchantmentHolders()) {
            if(ResourceId.parse(holder.getRegisteredName()).equals(id)) {
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    /**
     * {@code true} when child and parent enchantments can apply to at least one shared equipment slot.
     */
    static boolean slotsCompatible(Holder<Enchantment> child, Holder<Enchantment> parent) {
        List<EquipmentSlotGroup> childSlots = child.value().definition().slots();
        List<EquipmentSlotGroup> parentSlots = parent.value().definition().slots();
        for(EquipmentSlotGroup childGroup : childSlots) {
            for(EquipmentSlotGroup parentGroup : parentSlots) {
                if(slotGroupsOverlap(childGroup, parentGroup)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean slotGroupsOverlap(EquipmentSlotGroup a, EquipmentSlotGroup b) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            if(a.test(slot) && b.test(slot)) {
                return true;
            }
        }
        return false;
    }

    private static String formatCrossSlotReport(Holder<Enchantment> child, Holder<Enchantment> parent) {
        return child.getRegisteredName()
                + " [" + formatSlots(child.value().definition().slots()) + "]"
                + " depends_on "
                + parent.getRegisteredName()
                + " [" + formatSlots(parent.value().definition().slots()) + "]"
                + " (different gear slot)";
    }

    private static String formatSlots(List<EquipmentSlotGroup> slots) {
        return slots.stream()
                .map(EquipmentSlotGroup::getSerializedName)
                .collect(Collectors.joining(","));
    }

    private static Set<ResourceId> findCyclicNodes(Map<ResourceId, ResourceId> edges) {
        Set<ResourceId> cyclic = new HashSet<>();
        Set<ResourceId> visiting = new HashSet<>();
        Set<ResourceId> visited = new HashSet<>();

        for(ResourceId start : edges.keySet()) {
            dfs(start, edges, visiting, visited, cyclic);
        }
        return cyclic;
    }

    private static void dfs(
            ResourceId node,
            Map<ResourceId, ResourceId> edges,
            Set<ResourceId> visiting,
            Set<ResourceId> visited,
            Set<ResourceId> cyclic
    ) {
        if(visited.contains(node) || !edges.containsKey(node)) return;
        if(visiting.contains(node)) {
            cyclic.add(node);
            return;
        }
        visiting.add(node);
        ResourceId parent = edges.get(node);
        if(parent != null) {
            if(visiting.contains(parent)) {
                cyclic.add(node);
                cyclic.add(parent);
            } else {
                dfs(parent, edges, visiting, visited, cyclic);
                if(cyclic.contains(parent)) cyclic.add(node);
            }
        }
        visiting.remove(node);
        visited.add(node);
    }
}
