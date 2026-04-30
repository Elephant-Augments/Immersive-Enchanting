package me.alfie.immersiveenchanting.api.enchanting_tab;

@FunctionalInterface
public interface NodeClickHandler {

    /**
     * Called when a node is clicked in the UI.
     *
     * @param context The click context containing the node and UI environment.
     */
    void onClick(NodeClickContext context);
}
