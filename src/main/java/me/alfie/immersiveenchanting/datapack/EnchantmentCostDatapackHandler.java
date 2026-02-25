package me.alfie.immersiveenchanting.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.legacy.LegacyEnchantmentCost;
import me.alfie.immersiveenchanting.datapack.legacy.LevelCost;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentCostDatapackHandler extends SimpleJsonResourceReloadListener {

    private final Gson gson;
    public static final String DIRECTORY = "enchantment_costs";
    private MinecraftServer server;

    public EnchantmentCostDatapackHandler(Gson gson, String directory) {
        super(gson, directory);
        this.gson = gson;
    }

    /**
     * Set the server, so that client registries can be resynced
     * @param server
     */
    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Fires server-side.
     * Reads data pack from directory into the server's enchantment cost registry.
     * Triggers on /reload.
     * @param object
     * @param resourceManager
     * @param profiler
     */
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        //Lazily initialise the server enchantment cost registry.
        if(EnchantmentCostRegistry.getServerRegistry() == null) {
            EnchantmentCostRegistry.setServerRegistry(new EnchantmentCostRegistry());
        }
        EnchantmentCostRegistry.getServerRegistry().clear();

        //TODO
        //--- NEW FILE FORMAT ---///
        int fileCount = 0;
        for(Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation fileId = entry.getKey();   // e.g., immersiveenchanting:minecraft/efficiency
            JsonElement json = entry.getValue();

            //TODO
            //Skip these for now
            if(fileId.toString().equals("immersiveenchanting:test") || fileId.toString().equals("immersiveenchanting:lapis_cost")) {
                continue;
            }

            // Parse JSON into an EnchantmentCost
            EnchantmentCost enchantmentCost = parseJson(json);

            // Convert file path to actual enchantment RL: "minecraft/efficiency" → ResourceLocation("minecraft", "efficiency")
            String[] parts = fileId.getPath().split("/", 2);
            if (parts.length != 2) continue; // invalid file structure
            ResourceLocation enchantmentResourceLocation = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);

            // Put enchantment cost into registry
            //TODO: Change EnchantmentCostRegistry to use new system!
            //EnchantmentCostRegistry.getServerRegistry().getCostRegistry().put(ResourceKey.create(Registries.ENCHANTMENT, enchantmentResourceLocation), enchantmentCost);
            fileCount++;

            System.out.println(enchantmentCost.getCostNodeForLevel(2));

            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(Items.EMERALD, 5));
            items.add(new ItemStack(Items.DIAMOND, 5));
            items.add(new ItemStack(Items.OBSIDIAN, 15));
            boolean a = EnchantmentCost.isCostValid(enchantmentCost.getCostNodeForLevel(2),
                    items,
                    0);

            System.out.println(a);
        }
        ImmersiveEnchanting.LOGGER.info("Loaded " + fileCount + " enchantment costs.");
        //-----------------------///




        //TODO: Lapis cost to use enchanting fuel instead
        //Add lapis cost from data/immersiveenchanting/enchantment_costs/lapis_cost
        //Look for "lapis_cost.json"
        ResourceLocation lapisFileId = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "lapis_cost");
        JsonElement lapisJson = object.get(lapisFileId);

        EnchantmentCostRegistry.getServerRegistry().setLapisCost(new ItemStack(Items.AIR)); //Set to air by default
        if (lapisJson != null && lapisJson.isJsonObject()) {
            JsonObject lapisObj = lapisJson.getAsJsonObject();

            if (lapisObj.has("item") && lapisObj.has("amount")) {
                String itemString = lapisObj.get("item").getAsString(); // e.g., "minecraft:lapis_lazuli"
                int amount = lapisObj.get("amount").getAsInt();

                ResourceLocation itemRL = ResourceLocation.parse(itemString);
                Item item = BuiltInRegistries.ITEM.get(itemRL);
                if (item != Items.AIR) {
                    ItemStack lapisStack = new ItemStack(item, amount);
                    EnchantmentCostRegistry.getServerRegistry().setLapisCost(lapisStack);
                    ImmersiveEnchanting.LOGGER.info("Loaded lapis cost: " + lapisStack);
                }
            }
        }

        //Attempt to send sync packet to all players on reload
        int count = 0;
        if(server != null) {
            for(ServerPlayer player : server.getPlayerList().getPlayers()) {
                EnchantmentCostRegistrySyncPacket.syncClientWithServer(player);
                count++;
            }
            ImmersiveEnchanting.LOGGER.info("Synced server enchantment cost registry with " + count + " client(s).");
        }
    }


    /**
     * Parse an enchantment cost json, return as an EnchantmentCost.
     * @param file
     * @return
     */
    public static EnchantmentCost parseJson(JsonElement file) {
        ImmersiveEnchanting.LOGGER.info("Parsing JSON file...");

        JsonObject root = file.getAsJsonObject();
        
        //Check file has "levels"
        if(!root.has(ParserKey.LEVELS.getKey()) || !root.get(ParserKey.LEVELS.getKey()).isJsonObject()) {
            throw new EnchantmentCostParseException("Could not find 'levels' or 'levels' is not a JSON object in " + root);
        }

        //Search through each level key.
        JsonObject levels = root.getAsJsonObject(ParserKey.LEVELS.getKey());
        Map<String, CostNode> levelCosts = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : levels.entrySet()) {
            String levelKey = getLevelKey(entry);
            JsonObject levelNode = entry.getValue().getAsJsonObject();
            CostNode costNode = parseCostNode(levelNode);

            levelCosts.put(levelKey, costNode);
        }

        return new EnchantmentCost(levelCosts);
    }

    /**
     * Recursively parse through a level key.
     * @param node
     * @return
     */
    public static CostNode parseCostNode(JsonObject node) {
        if(node.has(ParserKey.ITEM.getKey()) || node.has(ParserKey.AMOUNT.getKey()) || node.has(ParserKey.XP_LEVELS.getKey())) {
            return parseCostLeaf(node);
        }

        else if (node.has(ParserKey.ANY_OF.getKey())) {
            JsonArray anyOfArray = node.getAsJsonArray(ParserKey.ANY_OF.getKey());
            List<CostNode> children = new ArrayList<>();
            for(JsonElement element : anyOfArray) {
                children.add(parseCostNode(element.getAsJsonObject())); //Recursive!
            }
            return new CostComposite(children, CompositeType.ANY_OF);
        } else if (node.has(ParserKey.ALL_OF.getKey())) {
            JsonArray allOfArray = node.getAsJsonArray((ParserKey.ALL_OF.getKey()));
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
        if(!costElement.has(ParserKey.ITEM.getKey())) {
            throw new EnchantmentCostParseException("Cost is missing 'item'");
        }
        String item = costElement.get(ParserKey.ITEM.getKey()).getAsString();

        //Optional contain "nbt"
        String nbt = "";
        if(costElement.has(ParserKey.NBT.getKey())) {
            nbt = costElement.get(ParserKey.NBT.getKey()).getAsString();
        }

        //Must contain "amount"
        if(!costElement.has(ParserKey.AMOUNT.getKey())) {
            throw new EnchantmentCostParseException("Cost is missing 'amount'");
        }
        int amount = costElement.get(ParserKey.AMOUNT.getKey()).getAsInt();

        //Optional contain "xp_levels"
        int xpLevels = 0;
        if(costElement.has(ParserKey.XP_LEVELS.getKey())) {
            xpLevels = costElement.get(ParserKey.XP_LEVELS.getKey()).getAsInt();
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

}
