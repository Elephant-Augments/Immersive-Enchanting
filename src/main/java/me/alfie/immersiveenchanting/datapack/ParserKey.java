package me.alfie.immersiveenchanting.datapack;

public enum ParserKey {
    LEVELS("levels"),
    ANY_OF("any_of"),
    ALL_OF("all_of"),
    ITEM("item"),
    AMOUNT("amount"),
    XP_LEVELS("xp_levels"),
    NBT("nbt");

    private final String key;

    ParserKey(String key) {
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
