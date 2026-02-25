package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.datapack.legacy.LegacyEnchantmentCost;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.legacy.LevelCost;
import me.alfie.immersiveenchanting.networking.ClientPayloadHandler;
import me.alfie.immersiveenchanting.networking.SerializedEnchantmentCostRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record EnchantmentCostRegistrySyncPacket(
        List<String> enchantmentNamespaces,
        List<String> levels,
        List<String> itemIds,
        List<Integer> amounts,
        String lapisCostItemId,
        int lapisCostAmount
    ) implements CustomPacketPayload {

    public static final Type<EnchantmentCostRegistrySyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "sync_enchantment_cost_registry_packet"));

    public static final StreamCodec<ByteBuf, EnchantmentCostRegistrySyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            EnchantmentCostRegistrySyncPacket::enchantmentNamespaces,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            EnchantmentCostRegistrySyncPacket::levels,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            EnchantmentCostRegistrySyncPacket::itemIds,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
            EnchantmentCostRegistrySyncPacket::amounts,
            ByteBufCodecs.STRING_UTF8,
            EnchantmentCostRegistrySyncPacket::lapisCostItemId,
            ByteBufCodecs.VAR_INT,
            EnchantmentCostRegistrySyncPacket::lapisCostAmount,
            EnchantmentCostRegistrySyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Serialize the cost registry.
     *
     * Map<ResourceLocation, LegacyEnchantmentCost>
     *     where LegacyEnchantmentCost contains:
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
        for(Map.Entry<ResourceKey<Enchantment>, LegacyEnchantmentCost> registryEntry : costRegistry.getCostRegistry().entrySet()) {
            ResourceKey<Enchantment> enchantmentKey = registryEntry.getKey();
            LegacyEnchantmentCost cost = registryEntry.getValue();

            //For each entry in the LegacyEnchantmentCost
            for(Map.Entry<String, LevelCost> costEntry : cost.levels.entrySet()) {
                String level = costEntry.getKey();
                LevelCost levelCost = costEntry.getValue();
                String itemNamespace = levelCost.item();
                int amount = levelCost.amount();

                //Add data to form parallel lists
                enchantmentNamespaces.add(enchantmentKey.location().toString());
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
            //Build LegacyEnchantmentCost
            //Only create a new enchantment cost instance if it doesn't exist yet
            LegacyEnchantmentCost legacyEnchantmentCost = enchantmentCostRegistry.getCostRegistry()
                    .computeIfAbsent(ResourceKey.create(Registries.ENCHANTMENT, enchantmentResourceLocation),k -> new LegacyEnchantmentCost());
            legacyEnchantmentCost.levels.put(level, levelCost);
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

        PacketDistributor.sendToPlayer(player, new EnchantmentCostRegistrySyncPacket(
                serializedRegistry.enchantmentNamespaces(),
                serializedRegistry.levels(),
                serializedRegistry.itemIds(),
                serializedRegistry.amounts(),
                serializedRegistry.lapisCostItemId(),
                serializedRegistry.lapisCostAmount()
        ));
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                EnchantmentCostRegistrySyncPacket.TYPE,
                EnchantmentCostRegistrySyncPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<EnchantmentCostRegistrySyncPacket>(
                        ClientPayloadHandler::onEnchantmentCostRegistrySync,
                        null
                )
        );
    }
}
