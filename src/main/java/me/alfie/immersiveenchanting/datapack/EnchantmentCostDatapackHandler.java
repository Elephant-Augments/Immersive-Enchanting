package me.alfie.immersiveenchanting.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.ImmersiveEnchantingEvents;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.datapack.cost.EnchantmentCost;
import me.alfie.immersiveenchanting.datapack.parser.DatapackParser;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        ImmersiveEnchanting.LOGGER.info("Parsing datapack files...");
        for(Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation fileId = entry.getKey();   // e.g., immersiveenchanting:minecraft/efficiency
            JsonElement json = entry.getValue();

            // Convert file path to actual enchantment RL: "minecraft/efficiency" → ResourceLocation("minecraft", "efficiency")
            String[] parts = fileId.getPath().split("/", 2);
            if (parts.length != 2) continue; // invalid file structure

            //Load transmute/replicate costs differently
            if(Objects.equals(parts[0], "immersiveenchanting")) {

                //Only level 1 is used for these costs
                EnchantmentCost enchantmentCost = DatapackParser.parseJson(json);

                EnchantmentCostRegistry.InternalCosts key;
                if(parts[1].equals("transmute")) {
                    key = EnchantmentCostRegistry.InternalCosts.TRANSMUTE;
                    EnchantmentCostRegistry.getServerRegistry().getInternalRegistry().put(key, enchantmentCost);
                } else if(parts[1].equals("replicate")) {
                    key = EnchantmentCostRegistry.InternalCosts.REPLICATE;
                    EnchantmentCostRegistry.getServerRegistry().getInternalRegistry().put(key, enchantmentCost);
                }



                ImmersiveEnchanting.LOGGER.info("Loaded costs for transmute and replicate.");

            //Normal enchantment costs
            } else {
                // Parse JSON into an EnchantmentCost
                EnchantmentCost enchantmentCost = DatapackParser.parseJson(json);
                ResourceLocation enchantmentResourceLocation = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);

                //Put into server registry
                EnchantmentCostRegistry.getServerRegistry().getCostRegistry().put(ResourceKey.create(Registries.ENCHANTMENT, enchantmentResourceLocation), enchantmentCost);
                fileCount++;
            }
        }
        ImmersiveEnchanting.LOGGER.info("Loaded " + fileCount + " enchantment costs.");
        //-----------------------///

        syncRegistry();
    }

    /**
     * Sync the server registry with the client.
     * !Server-side only
     */
    public void syncRegistry() {
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
     * Use the neoforge tag #neoforge:enchanting_fuels.
     * @return
     */
    public static List<Item> getValidEnchantingFuels() {
        return getItemsInTag(getItemTag("neoforge:enchanting_fuels"));
    }

    public static List<Item> getItemsInTag(TagKey<Item> itemTag) {
        return BuiltInRegistries.ITEM.getTag(itemTag)
                .map(tagSet -> tagSet.stream()
                        .map(Holder::value) // <-- convert Holder<Item> -> Item
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public static TagKey<Item> getItemTag(String resourceLocation) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(resourceLocation));
        return tag;
    }
}
