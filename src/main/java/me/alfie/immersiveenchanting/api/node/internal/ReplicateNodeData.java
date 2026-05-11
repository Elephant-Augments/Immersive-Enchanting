package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.networking.ReplicatePacket;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public record ReplicateNodeData() {

    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "replicate");

    public static NodeData<ReplicateNodeData> create() {
        return new NodeData<>(
                TYPE,
                new ReplicateNodeData(),
                (data, context) -> {
                    ClientPacketDistributor.sendToServer(new ReplicatePacket());
                }
        );
    }

}
