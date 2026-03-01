package me.alfie.immersiveenchanting.config;

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



    static {
        Pair<ServerConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ServerConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // Constructor takes only the builder
    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.push("gameplay"); // optional grouping

        // Define the config value here
        disableAncientBookRequirement = builder
                .comment("If enabled, Ancient Books are no longer required to unlock enchantments at the enchanting table. Enchantments still cost experience and materials as usual.")
                .translation("immersiveenchanting.config.disable_ancient_book_requirement")
                .define("disableAncientBookRequirement", false);

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
}
