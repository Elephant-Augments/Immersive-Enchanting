package me.alfie.immersiveenchanting.datapack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatapackParser {

    /**
     * Parse an enchantment cost json, return as an EnchantmentCost.
     * @param file
     * @return
     */
    public static EnchantmentCost parseJson(JsonElement file) {
        ImmersiveEnchanting.LOGGER.info("Parsing JSON file...");

        JsonObject root = file.getAsJsonObject();

        //Check file has "levels"
        if(!root.has(JsonProperty.LEVELS.getKey()) || !root.get(JsonProperty.LEVELS.getKey()).isJsonObject()) {
            throw new EnchantmentCostParseException("Could not find 'levels' or 'levels' is not a JSON object in " + root);
        }

        //Optional check enabled - default true
        boolean enabled = true;
        if(root.has(JsonProperty.ENABLED.getKey())) {
            enabled = root.get(JsonProperty.ENABLED.getKey()).getAsBoolean();
        }

        //Search through each level key.
        JsonObject levels = root.getAsJsonObject(JsonProperty.LEVELS.getKey());
        Map<String, CostNode> levelCosts = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : levels.entrySet()) {
            String levelKey = getLevelKey(entry);
            JsonObject levelNode = entry.getValue().getAsJsonObject();
            CostNode costNode = parseCostNode(levelNode);

            levelCosts.put(levelKey, costNode);
        }

        return new EnchantmentCost(levelCosts, enabled);
    }

    /**
     * Recursively parse through a level key.
     * @param node
     * @return
     */
    public static CostNode parseCostNode(JsonObject node) {
        if(node.has(JsonProperty.ITEM.getKey()) || node.has(JsonProperty.AMOUNT.getKey()) || node.has(JsonProperty.XP_LEVELS.getKey())) {
            return parseCostLeaf(node);
        }

        else if (node.has(JsonProperty.ANY_OF.getKey())) {
            JsonArray anyOfArray = node.getAsJsonArray(JsonProperty.ANY_OF.getKey());
            List<CostNode> children = new ArrayList<>();
            for(JsonElement element : anyOfArray) {
                children.add(parseCostNode(element.getAsJsonObject())); //Recursive!
            }
            return new CostComposite(children, CompositeType.ANY_OF);
        } else if (node.has(JsonProperty.ALL_OF.getKey())) {
            JsonArray allOfArray = node.getAsJsonArray((JsonProperty.ALL_OF.getKey()));
            List<CostNode> children = new ArrayList<>();
            for(JsonElement element : allOfArray) {
                children.add(parseCostNode(element.getAsJsonObject())); //Recursive!
            }
            ImmersiveEnchanting.LOGGER.warn("'all_of' is accepted in enchantment cost files but not currently supported!");
            return new CostComposite(children, CompositeType.ALL_OF);
        }

        else {
            throw new EnchantmentCostParseException("Invalid cost node.");
        }
    }

    public static CostLeaf parseCostLeaf(JsonObject costElement) {
        //Must contain "item"
        if(!costElement.has(JsonProperty.ITEM.getKey())) {
            throw new EnchantmentCostParseException("Cost is missing 'item'");
        }
        String item = costElement.get(JsonProperty.ITEM.getKey()).getAsString();

        //Optional contain "nbt"
        String nbt = "";
        if(costElement.has(JsonProperty.NBT.getKey())) {
            nbt = costElement.get(JsonProperty.NBT.getKey()).getAsString();
        }

        //Must contain "amount"
        if(!costElement.has(JsonProperty.AMOUNT.getKey())) {
            throw new EnchantmentCostParseException("Cost is missing 'amount'");
        }
        int amount = costElement.get(JsonProperty.AMOUNT.getKey()).getAsInt();

        //Optional contain "xp_levels"
        int xpLevels = 0;
        if(costElement.has(JsonProperty.XP_LEVELS.getKey())) {
            xpLevels = costElement.get(JsonProperty.XP_LEVELS.getKey()).getAsInt();
        }

        return new CostLeaf(item, nbt, amount, xpLevels);
    }


    /**
     * Level key must be a positive integer as a string, i.e "1", "2", etc.
     * This method throws an exception if the string is not a positive integer.
     * Otherwise, returns it.
     * @param entry
     * @return
     */
    private static @NotNull String getLevelKey(Map.Entry<String, JsonElement> entry) {
        String levelKey = entry.getKey();

        //Ensure levelKey is a positive integer.
        try {
            int level = Integer.parseInt(levelKey); //Throws if not an int
            if(level <= 0) {
                throw new EnchantmentCostParseException("Level must be a positive integer, got: " + levelKey);
            }
        } catch(NumberFormatException e) {
            throw new EnchantmentCostParseException("Level key must be an integer string, got: " + levelKey);
        }
        return levelKey;
    }

    public static JsonObject toJson(EnchantmentCost cost) {
        JsonObject root = new JsonObject();

        //Enabled flag
        root.addProperty(JsonProperty.ENABLED.getKey(), cost.enabled);

        //Levels object
        JsonObject levelsObject = new JsonObject();

        for(Map.Entry<String, CostNode> entry : cost.levels.entrySet()) {
            String level = entry.getKey();
            CostNode node = entry.getValue();

            levelsObject.add(level, serializeNode(node));
        }

        root.add("levels", levelsObject);
        return root;
    }

    private static JsonObject serializeNode(CostNode node) {
        JsonObject object = new JsonObject();

        if(node instanceof CostLeaf leaf) {
            object.addProperty(JsonProperty.ITEM.getKey(), leaf.item());

            if(!leaf.nbt().isEmpty()) {
                object.addProperty(JsonProperty.NBT.getKey(), leaf.nbt());
            }

            object.addProperty(JsonProperty.AMOUNT.getKey(), leaf.amount());

            if(leaf.xpLevels() > 0) {
                object.addProperty(JsonProperty.XP_LEVELS.getKey(), leaf.xpLevels());
            }
        } else if (node instanceof CostComposite composite) {
            JsonArray childrenArray = new JsonArray();

            for(CostNode child : composite.children()) {
                childrenArray.add(serializeNode(child));
            }

            if(composite.type() == CompositeType.ANY_OF) {
                object.add(JsonProperty.ANY_OF.getKey(), childrenArray);
            } else {
                object.add(JsonProperty.ALL_OF.getKey(), childrenArray);
            }
        }

        return object;
    }
}
