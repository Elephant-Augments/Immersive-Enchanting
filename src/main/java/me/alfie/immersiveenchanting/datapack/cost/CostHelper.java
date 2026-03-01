package me.alfie.immersiveenchanting.datapack.cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CostHelper {

    public static void expandCostGroupTagsRecursive(CostGroup costGroup) {
        costGroup.getCostItemTag().ifPresent(tag -> {
            expandCostGroupTag(costGroup);
        });

        for(CostDefinition child : costGroup.children()) {
            if(child instanceof CostGroup childGroup) {
                expandCostGroupTagsRecursive(childGroup);
            }
        }
    }

    private static void expandCostGroupTag(CostGroup costGroup) {
        if(costGroup.getCostItemTag().isPresent()) {
            CostItemTag costItemTag = costGroup.getCostItemTag().get();

            List<Item> itemsInTag = getItemsInItemTag(getItemTag(costItemTag.itemTag()));

            for(Item item : itemsInTag) {
                CostEntry costEntry = new CostEntry(item.toString(), "", costItemTag.amount(), costItemTag.xpLevels(), costItemTag);
                costGroup.children().add(costEntry);
            }
            ImmersiveEnchanting.LOGGER.info("Expanded {}", costGroup);
        }
    }


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
}
