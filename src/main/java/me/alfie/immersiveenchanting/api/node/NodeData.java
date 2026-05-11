package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class NodeData<T> {

    private final Identifier type;
    private final T value;
    private final BiConsumer<T, NodeClickContext> clickHandler;

    public NodeData(Identifier id, T value, BiConsumer<T, NodeClickContext> clickHandler) {
        this.type = id;
        this.value = value;
        this.clickHandler = clickHandler;
    }

    public Identifier type() {
        return type;
    }

    public T value() {
        return value;
    }

    public void onClick(NodeClickContext context) {
        clickHandler.accept(value, context);
    }
}
