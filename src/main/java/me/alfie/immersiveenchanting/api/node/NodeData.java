package me.alfie.immersiveenchanting.api.node;

import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

/**
 * Typed payload attached to a node, associating a type ResourceLocation, an arbitrary value,
 * and a click handler.
 *
 * <p>When the node is clicked, {@link #onClick} is called with the resolved value
 * and a {@link NodeClickContext} providing access to the screen and node.
 * The type ResourceLocation is used to dispatch description-layout extensions.
 *
 * @param <T> the type of the payload value
 */
public class NodeData<T extends NodePayload> {

    private final ResourceLocation type;
    private final T value;
    private final BiConsumer<T, NodeClickContext> clickHandler;

    public NodeData(ResourceLocation id, T value, BiConsumer<T, NodeClickContext> clickHandler) {
        this.type = id;
        this.value = value;
        this.clickHandler = clickHandler;
    }

    public ResourceLocation type() {
        return value.type();
    }

    public T value() {
        return value;
    }

    public void onClick(NodeClickContext context) {
        clickHandler.accept(value, context);
    }
}
