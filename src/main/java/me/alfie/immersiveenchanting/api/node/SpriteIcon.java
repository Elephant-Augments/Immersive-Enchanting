package me.alfie.immersiveenchanting.api.node;

import net.minecraft.resources.Identifier;

/**
 * A node icon rendered using a texture sprite.
 * <p>
 * The provided {@link Identifier} points to a texture resource that will be
 * used when rendering the node in the UI.
 *
 * @param id texture identifier used for rendering
 */
public record SpriteIcon(Identifier id) implements NodeIcon {
}
