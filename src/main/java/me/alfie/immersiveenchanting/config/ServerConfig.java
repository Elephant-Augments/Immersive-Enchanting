package me.alfie.immersiveenchanting.config;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    // Store the config properties as public finals
    public final ModConfigSpec.ConfigValue<Boolean> disableAncientBookRequirement;

    public final ModConfigSpec.ConfigValue<Integer> bookshelfSearchX;
    public final ModConfigSpec.ConfigValue<Integer> bookshelfSearchY;
    public final ModConfigSpec.ConfigValue<Integer> bookshelfSearchZ;


    public final ModConfigSpec.ConfigValue<Boolean> allowReplicate;
    public final ModConfigSpec.ConfigValue<Boolean> allowTransmute;

    public final ModConfigSpec.ConfigValue<Boolean> allowEnchantmentRemoval;

    public final ModConfigSpec.ConfigValue<Boolean> enableEnchantedBookTrades;
    public final ModConfigSpec.ConfigValue<Boolean> enableEnchantedBookLootTables;

    public final ModConfigSpec.ConfigValue<Boolean> obfuscateLockedEnchantments;


    static {
        Pair<ServerConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ServerConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // Constructor takes only the builder
    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.push("bookshelves");
        bookshelfSearchX = builder
                .comment("The number of blocks in the X-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_x") // translatable label
                .defineInRange("bookshelfSearchX", 2, 1, 8);
        bookshelfSearchY = builder
                .comment("The number of blocks in the Y-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_y") // translatable label
                .defineInRange("bookshelfSearchY", 3, 1, 8);
        bookshelfSearchZ = builder
                .comment("The number of blocks in the Z-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_z") // translatable label
                .defineInRange("bookshelfSearchZ", 2, 1, 8);
        builder.pop();

        builder.push("ancientbooks");
        disableAncientBookRequirement = builder
                .comment("If enabled, Ancient Books are no longer required to unlock enchantments at the enchanting table. Enchantments still cost experience and materials as usual.")
                .translation("immersiveenchanting.config.disable_ancient_book_requirement")
                .define("disableAncientBookRequirement", false);
        builder.pop();

        builder.push("enchantingtable");
        allowReplicate = builder
                .comment("Allow ancient books to be replicated in the enchanting table.")
                .translation("immersiveenchanting.config.allow_replicate")
                .define("allowReplicate", true);

        allowTransmute = builder
                .comment("Allow ancient books to be transmuted in the enchanting table.")
                .translation("immersiveenchanting.config.allow_transmute")
                .define("allowTransmute", true);

        allowEnchantmentRemoval = builder
                .comment("Allow enchantments to be removed in the enchanting table.")
                .translation("immersiveenchanting.config.allow_enchantment_removal")
                .define("allowEnchantmentRemoval", true);

        obfuscateLockedEnchantments = builder
                .comment("If enabled, enchantments that have not been found will have be obfuscated in the enchanting table.")
                .translation("immersiveenchanting.config.obfuscate_locked_enchantments")
                .define("obfuscateLockedEnchantments", true);
        builder.pop();

        builder.push("enchantedbooks");
        enableEnchantedBookLootTables = builder
                .comment("If enabled, vanilla enchanted books will spawn normally in loot tables such as chests.")
                .translation("immersiveenchanting.config.enable_enchanted_book_loot_tables")
                .define("enableEnchantedBookLootTables", false);

        enableEnchantedBookTrades = builder
                .comment("If enabled, vanilla enchanted books will appear in villager trades.")
                .comment("Note: You must /reload for changes to take effect for this option!")
                .translation("immersiveenchanting.config.enable_enchanted_book_trades")
                .define("enableEnchantedBookTrades", false);
        builder.pop();
    }

    /**
     * Returns true if ancient books are required, false if the disabled option is true.
     * @return
     */
    public static boolean areAncientBooksRequired() {
        return !ServerConfig.CONFIG.disableAncientBookRequirement.get();
    }

    public static int getBookshelfSearchX() {
        return ServerConfig.CONFIG.bookshelfSearchX.get();
    }

    public static int getBookshelfSearchY() {
        return ServerConfig.CONFIG.bookshelfSearchY.get();
    }

    public static int getBookshelfSearchZ() {
        return ServerConfig.CONFIG.bookshelfSearchZ.get();
    }



    public static boolean isAllowReplicate() {
        return ServerConfig.CONFIG.allowReplicate.get();
    }

    public static boolean isAllowTransmute() {
        return ServerConfig.CONFIG.allowTransmute.get();
    }

    public static boolean isEnchantmentRemovalAllowed() {
        return ServerConfig.CONFIG.allowEnchantmentRemoval.get();
    }

    public static boolean isAllowEnchantedBookLootTables() {
        return ServerConfig.CONFIG.enableEnchantedBookLootTables.get();
    }

    public static boolean isAllowEnchantedBookTrades() {
        try{
            return ServerConfig.CONFIG.enableEnchantedBookTrades.get();
        } catch (IllegalStateException e) { //Catch java.lang.IllegalStateException: Cannot get config value before config is loaded.
            return false;
        }
    }

    public static boolean isObfuscateLockedEnchantments() {
        return ServerConfig.CONFIG.obfuscateLockedEnchantments.get();
    }
}
