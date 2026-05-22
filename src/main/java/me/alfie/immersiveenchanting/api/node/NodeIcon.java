package me.alfie.immersiveenchanting.api.node;

/**
 * Sealed icon descriptor for a node. Either a {@link SpriteIcon} (texture atlas sprite)
 * or an {@link ItemIcon} (rendered item stack).
 */
public sealed interface NodeIcon permits SpriteIcon, ItemIcon {
}
