package me.alfie.immersiveenchanting.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

        //Add recipes from data/immersiveenchanting/enchantment_costs
        int recipeCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation fileId = entry.getKey();   // e.g., immersiveenchanting:minecraft/efficiency
            JsonElement json = entry.getValue();

            // Parse JSON into an EnchantmentCost
            EnchantmentCost enchantmentCost = gson.fromJson(json, EnchantmentCost.class);

            // Convert file path to actual enchantment RL: "minecraft/efficiency" → ResourceLocation("minecraft", "efficiency")
            String[] parts = fileId.getPath().split("/", 2);
            if (parts.length != 2) continue; // invalid file structure
            ResourceLocation enchantmentResourceLocation = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);

            //If empty json detected.
            //Add a dummy level cost with a DO NOT INCLUDE tag for the item field.
            if(enchantmentCost.levels.isEmpty()) {
                LevelCost levelCost = new LevelCost(LevelCost.DO_NOT_INCLUDE, 0);
                enchantmentCost.levels.put("-1", levelCost);
            }

            // Put enchantment cost into registry
            EnchantmentCostRegistry.getServerRegistry().getCostRegistry().put(enchantmentResourceLocation, enchantmentCost);

            recipeCount++;
        }
        ImmersiveEnchanting.LOGGER.info("Loaded " + recipeCount + " enchantment costs.");



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
        if(server != null) {
            for(ServerPlayer player : server.getPlayerList().getPlayers()) {
                EnchantmentCostRegistrySyncPacket.syncClientWithServer(player);
            }
            ImmersiveEnchanting.LOGGER.info("Synced server enchantment cost registry with all clients.");
        }
    }
}
