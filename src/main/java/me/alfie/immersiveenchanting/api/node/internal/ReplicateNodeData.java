package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.networking.ReplicatePacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public record ReplicateNodeData() implements NodePayload {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "replicate");

    public static NodeData<ReplicateNodeData> create() {
        return new NodeData<>(
                TYPE,
                new ReplicateNodeData(),
                (data, context) -> {
                    PacketDistributor.sendToServer(new ReplicatePacket());
                }
        );
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }
}
