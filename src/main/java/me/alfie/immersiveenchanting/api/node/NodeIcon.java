package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;

/**
 * Represents a visual icon rendered on a node in the enchanting UI.
 * <p>
 * Node icons define how a node is visually represented in the interface.
 * Implementations determine the rendering source, such as textures or item stacks.
 */
public sealed interface NodeIcon permits SpriteIcon, ItemIcon {
}
