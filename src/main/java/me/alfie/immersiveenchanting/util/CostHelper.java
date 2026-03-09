package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostDatapack;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.datapack.cost.CostGroup;
import me.alfie.immersiveenchanting.datapack.cost.GroupType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class CostHelper {
    public static List<Item> getItemsInItemTag(TagKey<Item> itemTag) {
        return BuiltInRegistries.ITEM.getTag(itemTag)
                .map(tagSet -> tagSet.stream()
                        .map(Holder::value) // <-- convert Holder<Item> -> Item
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Automatically removes the # prefix
     * @param itemTag
     * @return
     */
    public static TagKey<Item> getItemTag(String itemTag) {
        itemTag = itemTag.replaceFirst("#", "");
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(itemTag));
        return tag;
    }

    public static boolean isItemTag(String id) {
        return id.startsWith("#");
    }

    /**
     * Check if a list of items is a valid cost.
     * @param node
     * @param items
     * @param playerXp
     * @return
     */
    public static boolean isCostValid(CostDefinition node, List<ItemStack> items, int playerXp) {
        if(node instanceof CostEntry entry) {
            boolean hasItem = false;

            //Check item
            ItemStack costStack = entry.asItemStack();
            for(ItemStack stack : items) {
                if(stack.is(costStack.getItem())) {
                    //Check amount
                    if (stack.getCount() >= costStack.getCount()) {
                        hasItem = true;
                        break;
                    }
                }
            }

            //Check XP
            boolean hasXp = playerXp >= entry.xpLevels();
            return hasItem && hasXp;

        } else if(node instanceof CostGroup composite) {
            if(composite.type() == GroupType.ANY_OF) {
                //Any child is enough
                for(CostDefinition child : composite.children()) {
                    if(isCostValid(child, items, playerXp)) return true;
                }
                return false;
            } else {
                //All children must be valid
                for(CostDefinition child : composite.children()) {
                    if(!isCostValid(child, items, playerXp)) return false;
                }
                return true;
            }
        }

        //Never reached
        return false;
    }

    public static boolean isCostValid(CostEntry costEntry) {
        return costEntry != null;
    }

    public static boolean isEnchantingFuelValid(ItemStack enchantingFuel) {
        List<Item> validEnchantingFuels = EnchantmentCostDatapack.getValidEnchantingFuels();
        return validEnchantingFuels.contains(enchantingFuel.getItem());
    }

    /**
     * Searches the tree for a valid cost. Returns CostEntry if found.<br>
     * Returns null if no cost found.
     * @param node
     * @param items
     * @param playerXp
     * @return
     */
    @Nullable
    public static CostEntry findValidCost(CostDefinition node, List<ItemStack> items, int playerXp) {
        if(node == null) return CostEntry.EMPTY;

        if (node instanceof CostEntry entry) {
            // Check item
            ItemStack costStack = entry.asItemStack();
            for (ItemStack stack : items) {
                if (stack.is(costStack.getItem())) {
                    if (stack.getCount() >= costStack.getCount()) {
                        // Check XP
                        if (playerXp >= entry.xpLevels()) {
                            return entry;
                        }
                    }
                }
            }
            return null;

        } else if (node instanceof CostGroup composite) {
            if (composite.type() == GroupType.ANY_OF) {
                for (CostDefinition child : composite.children()) {
                    CostEntry result = findValidCost(child, items, playerXp);
                    if (result != null) {
                        return result; // first valid cost
                    }
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Deducts items/xp for a valid cost
     * @param validCost
     * @param itemStack
     * @param player
     */
    public static void deductCost(CostEntry validCost,
                                  ItemStack itemStack,
                                  Player player) {
        itemStack.shrink(validCost.amount());
        player.giveExperienceLevels(-validCost.xpLevels());
    }
}
