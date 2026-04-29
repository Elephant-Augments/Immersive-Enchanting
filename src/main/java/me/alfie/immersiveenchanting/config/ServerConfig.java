package me.alfie.immersiveenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3i;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.ConfigValue<Boolean> disableAncientBookRequirement;

    public final ModConfigSpec.ConfigValue<Integer> BOOKSHELF_SEARCH_X;
    public final ModConfigSpec.ConfigValue<Integer> BOOKSHELF_SEARCH_Y;
    public final ModConfigSpec.ConfigValue<Integer> BOOKSHELF_SEARCH_Z;

    public final ModConfigSpec.ConfigValue<Boolean> ALLOW_REPLICATE;
    public final ModConfigSpec.ConfigValue<Boolean> ALLOW_TRANSMUTE;

    public final ModConfigSpec.ConfigValue<Boolean> ALLOW_ENCHANTMENT_REMOVAL;

    //public final ModConfigSpec.ConfigValue<Boolean> enableEnchantedBookTrades;
    public final ModConfigSpec.ConfigValue<Boolean> ENABLE_ENCHANTED_BOOK_LOOT_TABLES;

    public final ModConfigSpec.ConfigValue<Boolean> OBFUSCATE_LOCKED_ENCHANTMENTS;


    static {
        Pair<ServerConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ServerConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // Constructor takes only the builder
    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.push("bookshelves");
        BOOKSHELF_SEARCH_X = builder
                .comment("The number of blocks in the X-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_x") // translatable label
                .defineInRange("bookshelfSearchX", 2, 2, 8);
        BOOKSHELF_SEARCH_Y = builder
                .comment("The number of blocks in the Y-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_y") // translatable label
                .defineInRange("bookshelfSearchY", 3, 1, 8);
        BOOKSHELF_SEARCH_Z = builder
                .comment("The number of blocks in the Z-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_z") // translatable label
                .defineInRange("bookshelfSearchZ", 2, 2, 8);
        builder.pop();

        builder.push("ancientbooks");
        disableAncientBookRequirement = builder
                .comment("If enabled, Ancient Books are no longer required to unlock enchantments at the enchanting table. Enchantments still cost experience and materials as usual.")
                .translation("immersiveenchanting.config.disable_ancient_book_requirement")
                .define("disableAncientBookRequirement", false);
        builder.pop();

        builder.push("enchantingtable");
        ALLOW_REPLICATE = builder
                .comment("Allow ancient books to be replicated in the enchanting table.")
                .translation("immersiveenchanting.config.allow_replicate")
                .define("allowReplicate", true);

        ALLOW_TRANSMUTE = builder
                .comment("Allow ancient books to be transmuted in the enchanting table.")
                .translation("immersiveenchanting.config.allow_transmute")
                .define("allowTransmute", true);

        ALLOW_ENCHANTMENT_REMOVAL = builder
                .comment("Allow enchantments to be removed in the enchanting table.")
                .translation("immersiveenchanting.config.allow_enchantment_removal")
                .define("allowEnchantmentRemoval", true);

        OBFUSCATE_LOCKED_ENCHANTMENTS = builder
                .comment("If enabled, enchantments that have not been found will have be obfuscated in the enchanting table.")
                .translation("immersiveenchanting.config.obfuscate_locked_enchantments")
                .define("obfuscateLockedEnchantments", true);
        builder.pop();

        builder.push("enchantedbooks");
        ENABLE_ENCHANTED_BOOK_LOOT_TABLES = builder
                .comment("If enabled, vanilla enchanted books will spawn normally in loot tables such as chests.")
                .translation("immersiveenchanting.config.enable_enchanted_book_loot_tables")
                .define("enableEnchantedBookLootTables", false);
    }

    public static boolean areAncientBooksRequired() {
        return !ServerConfig.CONFIG.disableAncientBookRequirement.get();
    }

    public static Vector3i getBookshelfSearchRadius() {
        return new Vector3i(ServerConfig.CONFIG.BOOKSHELF_SEARCH_X.get(),
                ServerConfig.CONFIG.BOOKSHELF_SEARCH_Y.get(),
                ServerConfig.CONFIG.BOOKSHELF_SEARCH_Z.get());
    }

    public static boolean isAllowReplicate() {
        return ServerConfig.CONFIG.ALLOW_REPLICATE.get();
    }

    public static boolean isAllowTransmute() {
        return ServerConfig.CONFIG.ALLOW_TRANSMUTE.get();
    }

    public static boolean isEnchantmentRemovalAllowed() {
        return ServerConfig.CONFIG.ALLOW_ENCHANTMENT_REMOVAL.get();
    }

    public static boolean isAllowEnchantedBookLootTables() {
        return ServerConfig.CONFIG.ENABLE_ENCHANTED_BOOK_LOOT_TABLES.get();
    }

    public static boolean isObfuscateLockedEnchantments() {
        return ServerConfig.CONFIG.OBFUSCATE_LOCKED_ENCHANTMENTS.get();
    }
}
