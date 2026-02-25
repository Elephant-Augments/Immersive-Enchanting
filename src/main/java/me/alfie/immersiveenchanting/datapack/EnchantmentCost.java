package me.alfie.immersiveenchanting.datapack;

import me.alfie.immersiveenchanting.datapack.legacy.LevelCost;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentCost {
    //String is the level number as a string - its a string because its from the json.
    public Map<String, CostNode> levels;
    public final boolean enabled;

    public EnchantmentCost(Map<String, CostNode> levelCosts) {
        this.enabled = true;

        this.levels = levelCosts;
    }

    public EnchantmentCost(Map<String, CostNode> levelCosts, boolean enabled) {
        this.enabled = enabled;

        this.levels = levelCosts;
    }

    /**
     * Helper function to getLevel using an int rather than a string.
     * @param level
     * @return
     */
    public CostNode getCostNodeForLevel(int level) {
        return levels.get(String.valueOf(level));
    }

    public int getHighestLevel() {
        if (levels == null || levels.isEmpty()) return 0;

        return levels.keySet().stream()
                .mapToInt(Integer::parseInt) // convert string keys to int
                .max()                       // find the largest level
                .orElse(0);                  // default if empty
    }

    public static List<CostLeaf> getRenderableAnyOfCosts(CostNode node) {
        List<CostLeaf> result = new ArrayList<>();

        if(node instanceof CostLeaf leaf) {
            result.add(leaf);
        } else if (node instanceof CostComposite composite) {
            if(composite.type() == CompositeType.ANY_OF) {
                for(CostNode child : composite.children()) {
                    result.addAll(getRenderableAnyOfCosts(child));
                }
            } else {
                //Ignore ALL_OF for now.
            }
        }

        return result;
    }



}
