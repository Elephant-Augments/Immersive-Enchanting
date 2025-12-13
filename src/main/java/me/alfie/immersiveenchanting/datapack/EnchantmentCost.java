package me.alfie.immersiveenchanting.datapack;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the costs for all levels of an enchantment.
 * For example, stores the cost of minecraft:efficiency from levels 1-5.
 */
public class EnchantmentCost {
    //String is the level number as a string - its a string because its from the json.
    public Map<String, LevelCost> levels = new HashMap<>();

    //Empty LevelCost containing 0 air.
    private static final LevelCost EMPTY = new LevelCost("minecraft:air", 0);

    public EnchantmentCost() {
    }


    /**
     * Helper function to getLevel using an int rather than a string.
     * @param level
     * @return
     */
    public LevelCost getLevel(int level) {
        return levels.getOrDefault(String.valueOf(level), EMPTY);
    }

    public int getHighestLevel() {
        if (levels == null || levels.isEmpty()) return 0;

        return levels.keySet().stream()
                .mapToInt(Integer::parseInt) // convert string keys to int
                .max()                       // find the largest level
                .orElse(0);                  // default if empty
    }

}
