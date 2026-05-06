package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.api.node.NodeIcon;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ModFilterNode extends Node {

    public ModFilterNode(Component title, int enchantmentLevel, Canvas canvas, NodeState state, NodeTier tier, @Nullable NodeIcon icon, NodeType type, NodeBranch parentBranch) {
        super(title, enchantmentLevel, canvas, state, tier, icon, type, parentBranch);
    }
}
