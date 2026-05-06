package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

public enum NodeType {
    /**Represents a node that is associated with an enchantment and will send an EnchantPacket*/
    ENCHANTMENT,

    /**Represents a node that is not an enchantment and has some other action*/
    ACTION,

    MOD_FILTER
}
