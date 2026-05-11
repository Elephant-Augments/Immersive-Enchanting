package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

/**
 * Represents typed data attached to a node in the enchanting UI.
 * <p>
 * Each {@code NodeData} instance defines:
 * <ul>
 *     <li>A stable type identifier used for registration or serialization</li>
 *     <li>The payload value associated with the node</li>
 *     <li>A click handler executed when the node is interacted with</li>
 * </ul>
 *
 * @param <T> type of the stored payload
 */
public class NodeData<T> {

    private final Identifier type;
    private final T value;
    private final BiConsumer<T, NodeClickContext> clickHandler;

    /**
     * Creates a new node data instance.
     *
     * @param id           stable identifier for this data type
     * @param value        payload associated with this node
     * @param clickHandler callback invoked when the node is clicked
     */
    public NodeData(Identifier id, T value, BiConsumer<T, NodeClickContext> clickHandler) {
        this.type = id;
        this.value = value;
        this.clickHandler = clickHandler;
    }

    /**
     * Represents data attached to a node in the enchanting UI.
     * <p>
     * The {@code type} field is a stable identifier used for
     * non-interaction systems such as visual effects, descriptions, sound effects,
     * and external UI filtering.
     * <p>
     * It is NOT used during click handling.
     */
    public Identifier type() {
        return type;
    }

    /**
     * Returns the payload stored in this node.
     *
     * @return the node's value
     */
    public T value() {
        return value;
    }

    /**
     * Invoked when the node is clicked.
     * <p>
     * Executes the registered click handler with the stored value
     * and the provided interaction context.
     *
     * @param context click interaction context
     */
    public void onClick(NodeClickContext context) {
        clickHandler.accept(value, context);
    }
}
