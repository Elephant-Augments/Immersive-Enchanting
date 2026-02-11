package me.alfie.immersiveenchanting.gui.transmute;

import me.alfie.immersiveenchanting.gui.core.Node;
import me.alfie.immersiveenchanting.gui.core.NodeType;
import net.minecraft.resources.ResourceLocation;

public class TransmuteNode extends Node {

    private boolean canTransmute;
    private boolean isBookReplicated;

    public TransmuteNode(NodeType nodeType, ResourceLocation iconTexture,
                         boolean canTransmute,
                         boolean isBookReplicated) {
        super(nodeType, iconTexture);
        setObtained(false);
        this.canTransmute = canTransmute;
        this.isBookReplicated = isBookReplicated;

        if (!canTransmute) {
            setNodeType(NodeType.LOCKED);
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
}
