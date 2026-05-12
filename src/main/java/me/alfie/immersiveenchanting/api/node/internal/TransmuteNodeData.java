package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.networking.TransmutePacket;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public record TransmuteNodeData() implements NodePayload {

    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "transmute");

    public static NodeData<TransmuteNodeData> create() {
        return new NodeData<>(
                TYPE,
                new TransmuteNodeData(),
                (data, context) -> {
                    ClientPacketDistributor.sendToServer(new TransmutePacket());
                }
        );
    }

    @Override
    public Identifier type() {
        return TYPE;
    }
}
