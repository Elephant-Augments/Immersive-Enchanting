package me.alfie.immersiveenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3i;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.ConfigValue<Boolean> DISABLE_ANCIENT_BOOK_REQUIREMENT;

    public final ModConfigSpec.ConfigValue<Boolean> SHOW_ALL_ENCHANTMENT_LEVELS;

    public final ModConfigSpec.ConfigValue<Integer> BOOKSHELF_SEARCH_X;
    public final ModConfigSpec.ConfigValue<Integer> BOOKSHELF_SEARCH_Y;
    public final ModConfigSpec.ConfigValue<Integer> BOOKSHELF_SEARCH_Z;

    public final ModConfigSpec.ConfigValue<Boolean> ALLOW_ENCHANTMENT_REMOVAL;

    public final ModConfigSpec.ConfigValue<Boolean> OBFUSCATE_LOCKED_ENCHANTMENTS;

    public final ModConfigSpec.ConfigValue<Boolean> DISABLE_ENCHANTED_BOOK_TRADES;

    public enum EnchantedBookLootMode {
        REPLACE,
        REMOVE
    }

    public final ModConfigSpec.EnumValue<EnchantedBookLootMode> ENCHANTED_BOOK_LOOT_MODE;

    public final ModConfigSpec.DoubleValue ALL_TABLES_CHANCE;
    public final ModConfigSpec.DoubleValue THEMED_TABLES_CHANCE;

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
                .comment("The number of blocks in the X-position that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_x") // translatable label
                .defineInRange("bookshelfSearchX", 2, 2, 8);
        BOOKSHELF_SEARCH_Y = builder
                .comment("The number of blocks in the Y-position that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_y") // translatable label
                .defineInRange("bookshelfSearchY", 3, 1, 8);
        BOOKSHELF_SEARCH_Z = builder
                .comment("The number of blocks in the Z-position that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_z") // translatable label
                .defineInRange("bookshelfSearchZ", 2, 2, 8);
        builder.pop();



        builder.push("enchantingtable");
        ALLOW_ENCHANTMENT_REMOVAL = builder
                .comment("Allow enchantments to be removed in the enchanting table.")
                .translation("immersiveenchanting.config.allow_enchantment_removal")
                .define("allowEnchantmentRemoval", true);

        OBFUSCATE_LOCKED_ENCHANTMENTS = builder
                .comment("If enabled, enchantments that have not been found will be obfuscated in the enchanting table.")
                .translation("immersiveenchanting.config.obfuscate_locked_enchantments")
                .define("obfuscateLockedEnchantments", true);

        DISABLE_ANCIENT_BOOK_REQUIREMENT = builder
                .comment("If enabled, Ancient Books are no longer required to unlock enchantments at the enchanting table. Enchantments still cost experience and materials as usual.")
                .translation("immersiveenchanting.config.disable_ancient_book_requirement")
                .define("disableAncientBookRequirement", false);

        SHOW_ALL_ENCHANTMENT_LEVELS = builder
                .comment("If enabled, every level of an enchantment is shown on the enchanting tree. If disabled, only obtained levels and the next unlearned level are shown.")
                .translation("immersiveenchanting.config.show_all_enchantment_levels")
                .define("showAllEnchantmentLevels", false);
        builder.pop();

        builder.push("enchantedbooks");
        ENCHANTED_BOOK_LOOT_MODE = builder
                .comment("Choose whether enchanted books should be removed completely or replaced with ancient books. Ancient books still spawn as normal.")
                .translation("immersiveenchanting.config.enchanted_book_loot_mode")
                .defineEnum("enchantedBookLootMode", EnchantedBookLootMode.REPLACE);

        DISABLE_ENCHANTED_BOOK_TRADES = builder
                .comment("If enabled, vanilla enchanted books will not appear in villager trades.")
                .translation("immersiveenchanting.config.disable_enchanted_book_trades")
                .define("disableEnchantedBookTrades", true);

        ALL_TABLES_CHANCE = builder
                .comment("Chance for the generic ancient-book loot pool to roll on chests. Default 0.1 (10%). End city and stronghold library use five times this value (50% at default).")
                .translation("immersiveenchanting.config.all_tables_chance")
                .defineInRange("allTablesChance", 0.1d, 0.0d, 1.0d);

        THEMED_TABLES_CHANCE = builder
                .comment("Chance for a themed ancient-book loot pool to roll on its matching chests. Default 0.25 (25%).")
                .translation("immersiveenchanting.config.themed_tables_chance")
                .defineInRange("themedTablesChance", 0.25d, 0.0d, 1.0d);
        builder.pop();
    }

    public static boolean areAncientBooksRequired() {
        return !ServerConfig.CONFIG.DISABLE_ANCIENT_BOOK_REQUIREMENT.get();
    }

    public static Vector3i getBookshelfSearchRadius() {
        return new Vector3i(ServerConfig.CONFIG.BOOKSHELF_SEARCH_X.get(),
                ServerConfig.CONFIG.BOOKSHELF_SEARCH_Y.get(),
                ServerConfig.CONFIG.BOOKSHELF_SEARCH_Z.get());
    }

    public static boolean isEnchantmentRemovalAllowed() {
        return ServerConfig.CONFIG.ALLOW_ENCHANTMENT_REMOVAL.get();
    }

    public static boolean isEnchantedBookLootMode(EnchantedBookLootMode mode) {
        return ServerConfig.CONFIG.ENCHANTED_BOOK_LOOT_MODE.get() == mode;
    }

    public static boolean areEnchantedBookTradesDisabled() {
        return ServerConfig.CONFIG.DISABLE_ENCHANTED_BOOK_TRADES.get();
    }

    public static boolean isObfuscateLockedEnchantments() {
        return ServerConfig.CONFIG.OBFUSCATE_LOCKED_ENCHANTMENTS.get();
    }

    public static boolean showAllEnchantmentLevels() {
        return ServerConfig.CONFIG.SHOW_ALL_ENCHANTMENT_LEVELS.get();
    }

    public static double getAllTablesChance() {
        return ServerConfig.CONFIG.ALL_TABLES_CHANCE.get();
    }

    public static double getThemedTablesChance() {
        return ServerConfig.CONFIG.THEMED_TABLES_CHANCE.get();
    }
}
