package me.alfie.immersiveenchanting.datapack;

public enum JsonProperty {
    LEVELS("levels"),
    ANY_OF("any_of"),
    ALL_OF("all_of"),
    ITEM("item"),
    AMOUNT("amount"),
    XP_LEVELS("xp_levels"),
    ENABLED("enabled"),
    NBT("nbt");

    private final String key;

    JsonProperty(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
