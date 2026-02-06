package me.alfie.immersiveenchanting.gui.replicate;

import me.alfie.immersiveenchanting.gui.core.Node;
import me.alfie.immersiveenchanting.gui.core.NodeType;
import net.minecraft.resources.ResourceLocation;

public class ReplicateNode extends Node {

    public ReplicateNode(NodeType nodeType, ResourceLocation iconTexture) {
        super(nodeType, iconTexture);
        setObtained(false);
    }
}
