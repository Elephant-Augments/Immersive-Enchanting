package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.Sprite;

public enum NodeState {
    UNOBTAINED(Sprite.BASIC_NODE_UNOBTAINED, Sprite.ADVANCED_NODE_UNOBTAINED, Sprite.ELITE_NODE_UNOBTAINED),
    OBTAINED(Sprite.BASIC_NODE_OBTAINED, Sprite.ADVANCED_NODE_OBTAINED, Sprite.ELITE_NODE_OBTAINED),
    LOCKED(Sprite.LOCKED_NODE),
    ALERT(Sprite.ALERT_NODE);

    private Sprite basic;
    private Sprite advanced;
    private Sprite elite;

    NodeState(Sprite basic, Sprite advanced, Sprite elite) {
        this.basic = basic;
        this.advanced = advanced;
        this.elite = elite;
    }

    NodeState(Sprite sprite) {
        this.basic = sprite;
        this.advanced = sprite;
        this.elite = sprite;
    }

    public Sprite getSpriteForTier(NodeTier tier) {
        return switch (tier) {
            case BASIC -> basic;
            case ADVANCED -> advanced;
            case ELITE -> elite;
        };
    }
}
