package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.replicate;

import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeType;
import me.alfie.immersiveenchanting.networking.ModPackets;
import me.alfie.immersiveenchanting.networking.packet.replicatebookpacket.ReplicateBookPacket;
import net.minecraft.resources.ResourceLocation;

public class ReplicateNode extends Node {

    public ReplicateNode(NodeType nodeType, ResourceLocation iconTexture) {
        super(nodeType, iconTexture);
        setObtained(false);
    }

    @Override
    public boolean onClicked(int mouseX, int mouseY) {
        ModPackets.INSTANCE.sendToServer(new ReplicateBookPacket());
        return true;
    }
}
