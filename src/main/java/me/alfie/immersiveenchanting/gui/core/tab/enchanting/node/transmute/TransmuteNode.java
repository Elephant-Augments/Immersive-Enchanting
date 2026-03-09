package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.transmute;

import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeType;
import me.alfie.immersiveenchanting.networking.packet.transmutebookpacket.TransmuteBookPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class TransmuteNode extends Node {

    private final boolean canTransmute;
    private final boolean isBookReplicated;

    public TransmuteNode(NodeType nodeType, ResourceLocation iconTexture,
                         boolean canTransmute,
                         boolean isBookReplicated) {
        super(nodeType, iconTexture);
        setObtained(false);
        this.canTransmute = canTransmute;
        this.isBookReplicated = isBookReplicated;

        if (!canTransmute) {
            setNodeType(NodeType.LOCKED);
            if(isBookReplicated) {
                setNodeType(NodeType.ALERT);
            }
            setIconTexture(null);
        }
    }

    /**
     * Is transmuting possible (Matching book in bookshelf and not a replica)
     * @return
     */
    public boolean canTransmute() {
        return canTransmute;
    }

    public boolean isBookReplicated() {
        return isBookReplicated;
    }

    @Override
    public boolean onClicked(int mouseX, int mouseY) {
        if(canTransmute()) {
            PacketDistributor.sendToServer(new TransmuteBookPacket(0));
            return true;
        }
        return false;
    }
}
