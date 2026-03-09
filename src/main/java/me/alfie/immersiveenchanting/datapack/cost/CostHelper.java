package me.alfie.immersiveenchanting.datapack.cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.ArrayList;
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



    public static String toAsciiTree(CostDefinition root) {
        StringBuilder builder = new StringBuilder();
        buildAscii(root, builder, "", true);
        return builder.toString();
    }

    private static void buildAscii(CostDefinition node,
                                   StringBuilder builder,
                                   String prefix,
                                   boolean isLast) {

        builder.append(prefix)
                .append(isLast ? "└── " : "├── ");

        if (node instanceof CostEntry entry) {

            builder.append("ITEM ")
                    .append(entry.item())
                    .append(" x")
                    .append(entry.amount());

            if (entry.xpLevels() > 0) {
                builder.append(" (xp: ").append(entry.xpLevels()).append(")");
            }

            if (!entry.nbt().isEmpty()) {
                builder.append(" [nbt]");
            }

            builder.append("\n");

        } else if (node instanceof CostGroup group) {

            builder.append(group.type());

            group.getCostItemTag()
                    .ifPresent(tag -> builder.append(" {tag ").append(tag.itemTag()).append("}"));

            builder.append("\n");

            var children = group.children();

            for (int i = 0; i < children.size(); i++) {
                boolean childLast = (i == children.size() - 1);

                buildAscii(
                        children.get(i),
                        builder,
                        prefix + (isLast ? "    " : "│   "),
                        childLast
                );
            }
        }
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
}
