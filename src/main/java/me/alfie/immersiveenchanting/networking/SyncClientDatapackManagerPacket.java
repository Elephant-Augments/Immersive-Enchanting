package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record SyncClientDatapackManagerPacket(CostRegistry costRegistry,
                                              NodeSoundMap nodeSoundMap) implements ModNetworkPacket {

    public static final PacketCodec<SyncClientDatapackManagerPacket> CODEC = new PacketCodec<SyncClientDatapackManagerPacket>() {
        @Override
        public void encode(SyncClientDatapackManagerPacket packet, FriendlyByteBuf buf) {
            CostRegistry.STREAM_CODEC.encode(buf, packet.costRegistry());
            NodeSoundMap.STREAM_CODEC.encode(buf, packet.nodeSoundMap());
        }

        @Override
        public SyncClientDatapackManagerPacket decode(FriendlyByteBuf buf) {
            return new SyncClientDatapackManagerPacket(
                    CostRegistry.STREAM_CODEC.decode(buf),
                    NodeSoundMap.STREAM_CODEC.decode(buf)
            );
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isClient()) return;

        CostRegistry costRegistry = costRegistry();
        NodeSoundMap nodeSoundMap = nodeSoundMap();

        LocalPlayer player = Minecraft.getInstance().player;
        ClientDatapackManager.setCostRegistry(costRegistry, player.level().registryAccess());
        ClientDatapackManager.setNodeSoundMap(nodeSoundMap);

        ImmersiveEnchanting.LOGGER.debug("Received sync packet on client, updated client datapack manager.");
        costRegistry.printRegistry();
    }
}

