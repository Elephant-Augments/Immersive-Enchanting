package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.networking.TransmutePacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public record TransmuteNodeData() implements NodePayload {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "transmute");

    public static NodeData<TransmuteNodeData> create() {
        return new NodeData<>(
                TYPE,
                new TransmuteNodeData(),
                (data, context) -> {
                    PacketDistributor.sendToServer(new TransmutePacket());
                }
        );
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }
}
