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

    /**
     * Check if a list of items is a valid cost.
     * @param node
     * @param items
     * @param playerXp
     * @return
     */
    public static boolean isCostValid(CostNode node, List<ItemStack> items, int playerXp) {
        if(node instanceof CostLeaf leaf) {
            boolean hasItem = false;

            //Check item
            ItemStack leafStack = leaf.asItemStack();
            for(ItemStack stack : items) {
                if(stack.is(leafStack.getItem())) {
                    //Check amount
                    if (stack.getCount() >= leafStack.getCount()) {
                        hasItem = true;
                        break;
                    }
                }
            }

            //Check XP
            boolean hasXp = playerXp >= leaf.xpLevels();
            return hasItem && hasXp;

        } else if(node instanceof CostComposite composite) {
            if(composite.type() == CompositeType.ANY_OF) {
                //Any child is enough
                for(CostNode child : composite.children()) {
                    if(isCostValid(child, items, playerXp)) return true;
                }
                return false;
            } else {
                //All children must be valid
                for(CostNode child : composite.children()) {
                    if(!isCostValid(child, items, playerXp)) return false;
                }
                return true;
            }
        }

        //Never reached
        return false;
    }

}
