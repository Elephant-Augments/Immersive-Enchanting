package me.alfie.immersiveenchanting.gui.transmute;

import me.alfie.immersiveenchanting.gui.core.Node;
import me.alfie.immersiveenchanting.gui.core.NodeType;
import net.minecraft.resources.ResourceLocation;

public class TransmuteNode extends Node {

    private boolean canTransmute;

    public TransmuteNode(NodeType nodeType, ResourceLocation iconTexture, boolean canTransmute) {
        super(nodeType, iconTexture);
        setObtained(false);
        this.canTransmute = canTransmute;

        if (!canTransmute) {
            setNodeType(NodeType.LOCKED);
            setIconTexture(null);
        }
    }

    public boolean canTransmute() {
        return canTransmute;
    }
}
