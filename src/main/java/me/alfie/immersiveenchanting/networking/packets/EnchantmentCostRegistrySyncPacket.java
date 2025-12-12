package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.datapack.EnchantmentCost;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.LevelCost;
import me.alfie.immersiveenchanting.networking.ClientPayloadHandler;
import me.alfie.immersiveenchanting.networking.ModPacketHandler;
import me.alfie.immersiveenchanting.networking.SerializedEnchantmentCostRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EnchantmentCostRegistrySyncPacket {

    public final List<String> enchantmentNamespaces;
    public final List<String> levels;
    public final List<String> itemIds;
    public final List<Integer> amounts;
    public final String lapisCostItemId;
    public final int lapisCostAmount;

    public EnchantmentCostRegistrySyncPacket(
            List<String> enchantmentNamespaces,
            List<String> levels,
            List<String> itemIds,
            List<Integer> amounts,
            String lapisCostItemId,
            int lapisCostAmount) {
        this.enchantmentNamespaces = enchantmentNamespaces;
        this.levels = levels;
        this.itemIds = itemIds;
        this.amounts = amounts;
        this.lapisCostItemId = lapisCostItemId;
        this.lapisCostAmount = lapisCostAmount;
    }

    public static void encode(EnchantmentCostRegistrySyncPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.enchantmentNamespaces, (b, str) -> b.writeUtf(str));
        buf.writeCollection(packet.levels, (b, str) -> b.writeUtf(str));
        buf.writeCollection(packet.itemIds, (b, str) -> b.writeUtf(str));
        buf.writeCollection(packet.amounts, (b, integer) -> b.writeInt(integer));
        buf.writeUtf(packet.lapisCostItemId);
        buf.writeInt(packet.lapisCostAmount);
    }

    public static EnchantmentCostRegistrySyncPacket decode(FriendlyByteBuf buf) {
        List<String> enchantmentNamespaces = buf.readList(b -> b.readUtf());
        List<String> levels = buf.readList(b -> b.readUtf());
        List<String> itemIds = buf.readList(b -> b.readUtf());
        List<Integer> amounts = buf.readList(b -> b.readInt());
        String lapisCostItemId = buf.readUtf();
        int lapisCostAmount = buf.readInt();
        return new EnchantmentCostRegistrySyncPacket(
                enchantmentNamespaces,
                levels,
                itemIds,
                amounts,
                lapisCostItemId,
                lapisCostAmount
        );
    }

    public static void handle(EnchantmentCostRegistrySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> {
                    ClientPayloadHandler.onEnchantmentCostRegistrySync(packet, contextSupplier.get());
                });
        contextSupplier.get().setPacketHandled(true);
    }

        /**
         * Serialize the cost registry.
         *
         * Map<ResourceLocation, EnchantmentCost>
         *     where EnchantmentCost contains:
         *         Map<String, LevelCost> (String is a number representing the level)
         *             where LevelCost is:
         *                 item: String
         *                 amount: int
         * @param costRegistry
         */
    public static SerializedEnchantmentCostRegistry serialize(EnchantmentCostRegistry costRegistry) {
        List<String> enchantmentNamespaces = new ArrayList<>();
        List<String> levels = new ArrayList<>();
        List<String> itemNamespaces = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        //For each entry in the EnchantmentCostRegistry
        for(Map.Entry<ResourceLocation, EnchantmentCost> registryEntry : costRegistry.getCostRegistry().entrySet()) {
            ResourceLocation enchantmentResourceLocation = registryEntry.getKey();
            EnchantmentCost cost = registryEntry.getValue();

            //For each entry in the EnchantmentCost
            for(Map.Entry<String, LevelCost> costEntry : cost.levels.entrySet()) {
                String level = costEntry.getKey();
                LevelCost levelCost = costEntry.getValue();
                String itemNamespace = levelCost.item();
                int amount = levelCost.amount();

                //Add data to form parallel lists
                enchantmentNamespaces.add(enchantmentResourceLocation.toString());
                levels.add(level);
                itemNamespaces.add(itemNamespace);
                amounts.add(amount);
            }
        }
        return new SerializedEnchantmentCostRegistry(
                enchantmentNamespaces, levels, itemNamespaces, amounts,
                costRegistry.getLapisCost().getItem().toString(),
                costRegistry.getLapisCost().getCount()
        );
    }

    /**
     * Deserialize a SerializedEnchantmentCostRegistry object, return an EnchantmentCostRegistry object.
     * @param serializedRegistry
     * @return
     */
    public static EnchantmentCostRegistry deserialize(SerializedEnchantmentCostRegistry serializedRegistry) {
        EnchantmentCostRegistry enchantmentCostRegistry = new EnchantmentCostRegistry();

        //For each namespace in the parallel list
        for (int i = 0; i < serializedRegistry.enchantmentNamespaces().size(); i++) {
            ResourceLocation enchantmentResourceLocation = ResourceLocation.parse(serializedRegistry.enchantmentNamespaces().get(i));
            String level = serializedRegistry.levels().get(i);
            String item = serializedRegistry.itemIds().get(i);
            int amount = serializedRegistry.amounts().get(i);

            //Build LevelCost
            LevelCost levelCost = new LevelCost(item, amount);
            //Build EnchantmentCost
            //Only create a new enchantment cost instance if it doesn't exist yet
            EnchantmentCost enchantmentCost = enchantmentCostRegistry.getCostRegistry().computeIfAbsent(enchantmentResourceLocation, k -> new EnchantmentCost());
            enchantmentCost.levels.put(level, levelCost);
        }

        return enchantmentCostRegistry;
    }

    /**
     * Warning! Ensure this is only fired server-side!
     * @param player
     */
    public static void syncClientWithServer(ServerPlayer player) {
        if(player.level().isClientSide) return; //Disallow client running

        //Request the server to send the serverEnchantmentCostRegistry
        //Serialize the registry
        SerializedEnchantmentCostRegistry serializedRegistry = EnchantmentCostRegistrySyncPacket.serialize(EnchantmentCostRegistry.getServerRegistry());

        ModPacketHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new EnchantmentCostRegistrySyncPacket(
                        serializedRegistry.enchantmentNamespaces(),
                        serializedRegistry.levels(),
                        serializedRegistry.itemIds(),
                        serializedRegistry.amounts(),
                        serializedRegistry.lapisCostItemId(),
                        serializedRegistry.lapisCostAmount()
                ));
    }
}
