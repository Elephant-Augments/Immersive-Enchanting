package me.alfie.immersiveenchanting.datapack.cost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantmentCost {
    //String is the level number as a string - its a string because its from the json.
    public Map<String, CostDefinition> levels;
    public final boolean enabled;

    public EnchantmentCost(Map<String, CostDefinition> levelCosts) {
        this.enabled = true;

        this.levels = levelCosts;
    }

    public EnchantmentCost(Map<String, CostDefinition> levelCosts, boolean enabled) {
        this.enabled = enabled;

        this.levels = levelCosts;
    }

    public List<CostDefinition> getAllLevels() {
        List<CostDefinition> costs = new ArrayList<>();
        int highestLevel = getHighestLevel();
        for (int i = 0; i < highestLevel; i++) {
            costs.add(levels.get(String.valueOf(i)));
        }
        return costs;
    }

    public EnchantmentCost resolveTags() {
        Map<String, CostDefinition> resolvedLevels = levels.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().resolveTags() // replaces tagged CostEntry with CostGroup
                ));

        return new EnchantmentCost(resolvedLevels, enabled);
    }

    /**
     * Helper function to getLevel using an int rather than a string.
     * @param level
     * @return
     */
    public CostDefinition getCostNodeForLevel(int level) {
        return levels.get(String.valueOf(level));
    }

    public int getHighestLevel() {
        if (levels == null || levels.isEmpty()) return 0;

        return levels.keySet().stream()
                .mapToInt(Integer::parseInt) // convert string keys to int
                .max()                       // find the largest level
                .orElse(0);                  // default if empty
    }

    public static List<CostEntry> getRenderableAnyOfCosts(CostDefinition node) {
        List<CostEntry> result = new ArrayList<>();

        if(node instanceof CostEntry leaf) {
            result.add(leaf);
        } else if (node instanceof CostGroup composite) {
            if(composite.type() == GroupType.ANY_OF) {
                for(CostDefinition child : composite.children()) {
                    result.addAll(getRenderableAnyOfCosts(child));
                }
            } else {
                //Ignore ALL_OF for now.
            }
        }

        return result;
    }



}
