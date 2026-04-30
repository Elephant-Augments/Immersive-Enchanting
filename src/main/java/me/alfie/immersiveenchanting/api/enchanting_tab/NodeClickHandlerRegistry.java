package me.alfie.immersiveenchanting.api.enchanting_tab;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.networking.ReplicatePacket;
import me.alfie.immersiveenchanting.networking.TransmutePacket;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class NodeClickHandlerRegistry {

    private static final Map<Identifier, NodeClickHandler> HANDLERS = new HashMap<>();

    public static void register(Identifier id, NodeClickHandler handler) {
        HANDLERS.put(id, handler);
    }

    public static void handle(NodeClickContext context) {
        NodeClickHandler handler = HANDLERS.get(context.node().id());

        if (handler == null) {
            ImmersiveEnchanting.LOGGER.error("[NodeClickHandlerRegistry] No click handler registered for node: {}", context.node().id());
        } else {
            handler.onClick(context);
        }
    }

    public static void registerInternalNodeInteractions(RegisterNodeClickHandlerEvent event) {
        event.register(CostRegistry.REPLICATE, clickHandler -> {
            ClientPacketDistributor.sendToServer(new ReplicatePacket());
        });

        event.register(CostRegistry.TRANSMUTE, clickHandler -> {
            ClientPacketDistributor.sendToServer(new TransmutePacket());
        });
    }
}
