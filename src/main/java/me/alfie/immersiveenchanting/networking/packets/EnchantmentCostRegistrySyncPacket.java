package me.alfie.immersiveenchanting.networking.packets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.ImmersiveEnchantingEvents;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.datapack.parser.DatapackParser;
import me.alfie.immersiveenchanting.datapack.cost.EnchantmentCost;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
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
        List<String> enchantmentIds,
        List<String> jsonStrings) implements CustomPacketPayload {

    public static final Type<EnchantmentCostRegistrySyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "sync_enchantment_cost_registry_packet"));

    public static final StreamCodec<ByteBuf, EnchantmentCostRegistrySyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            EnchantmentCostRegistrySyncPacket::enchantmentIds,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            EnchantmentCostRegistrySyncPacket::jsonStrings,
            EnchantmentCostRegistrySyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    /**
     * Convert the enchantment cost registry into a serialized object.
     * @param registry
     * @return
     */
    public static SerializedEnchantmentCostRegistry serialize(EnchantmentCostRegistry registry) {
        Map<ResourceKey<Enchantment>, EnchantmentCost> costRegistry = registry.getCostRegistry();

        List<String> enchantmentIds = new ArrayList<>();
        List<String> jsonStrings = new ArrayList<>();

        for(Map.Entry<ResourceKey<Enchantment>, EnchantmentCost> entry : costRegistry.entrySet()) {
            ResourceKey<Enchantment> enchantmentResourceKey = entry.getKey();
            EnchantmentCost cost = entry.getValue();

            //EnchantmentKey to string
            String enchantmentId = enchantmentResourceKey.location().toString();

            //Cost to JSON
            JsonObject json = DatapackParser.toJson(cost);
            String jsonString = json.toString();

            enchantmentIds.add(enchantmentId);
            jsonStrings.add(jsonString);
        }

        return new SerializedEnchantmentCostRegistry(
                enchantmentIds,
                jsonStrings
        );
    }

    public static EnchantmentCostRegistry deserialize(SerializedEnchantmentCostRegistry serializedRegistry) {
        EnchantmentCostRegistry registry = new EnchantmentCostRegistry();

        List<String> enchantmentIds = serializedRegistry.enchantmentIds();
        List<String> jsonStrings = serializedRegistry.jsonStrings();

        if(enchantmentIds.size() != jsonStrings.size()) {
            throw new IllegalStateException("Serialized registry lists have different sizes, cannot deserialize.");
        }

        for (int i = 0; i < enchantmentIds.size(); i++) {
            String enchantmentId = enchantmentIds.get(i);
            String jsonString = jsonStrings.get(i);

            //Parse JSON string
            JsonElement element = JsonParser.parseString(jsonString);
            EnchantmentCost cost = DatapackParser.parseJson(element);

            //Id to RL
            ResourceLocation resourceLocation = ResourceLocation.parse(enchantmentId);
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, resourceLocation);

            //Put in reg
            registry.getCostRegistry().put(key, cost);
        }


        return registry;
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
                serializedRegistry.enchantmentIds(),
                serializedRegistry.jsonStrings()));
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
