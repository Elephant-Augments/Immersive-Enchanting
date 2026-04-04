package me.alfie.immersiveenchanting.datapack;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record EnchantmentCostLevels(Map<Integer, EnchantmentCostHolder> levelCostMap) {

    public static final Codec<EnchantmentCostLevels> CODEC =
            Codec.unboundedMap(
                    Codec.STRING,  // Read JSON keys as strings
                    EnchantmentCostHolder.CODEC
            ).xmap(
                    map -> {
                        // Convert string keys to int
                        Map<Integer, EnchantmentCostHolder> intMap = map.entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> Integer.parseInt(e.getKey()),
                                        Map.Entry::getValue
                                ));
                        return new EnchantmentCostLevels(intMap);
                    },
                    levels -> {
                        // Convert int keys back to strings when writing JSON
                        return levels.levelCostMap().entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> e.getKey().toString(),
                                        Map.Entry::getValue
                                ));
                    }
            );


    public EnchantmentCostHolder getLevel(int level) {
        return levelCostMap.get(level);
    }

    public int maxLevel() {
        return Collections.max(levelCostMap.keySet());
    }

    /**
     * Returns a list of all valid costs for this enchantment, ignoring the level.
     * @return
     */
    public List<EnchantmentCost> getAllLevels() {
        List<EnchantmentCost> result = new ArrayList<>();

        for(EnchantmentCostHolder holder : levelCostMap().values()) {
            result.addAll(holder.costs());
        }

        return result;
    }

}
