package me.alfie.immersiveenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    // Store the config properties as public finals
    public final ModConfigSpec.ConfigValue<Boolean> disableAncientBookRequirement;
    public final ModConfigSpec.ConfigValue<Integer> bookshelfSearchHeight;



    static {
        Pair<ServerConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ServerConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // Constructor takes only the builder
    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.push("general"); // optional grouping

        // Define the config value here
        disableAncientBookRequirement = builder
                .comment("If enabled, Ancient Books are no longer required to unlock enchantments at the enchanting table. Enchantments still cost experience and materials as usual.")
                .translation("immersiveenchanting.config.disable_ancient_book_requirement")
                .define("disableAncientBookRequirement", false);

        bookshelfSearchHeight = builder
                .comment("The number of blocks in the Y-level that the enchanting table can detect chiseled bookshelves. Each 'row' can hold up to 96 books. If you have many enchantments, you may need to increase this value to provide more space.") // translatable comment
                .translation("immersiveenchanting.config.bookshelf_search_height") // translatable label
                .defineInRange("bookshelfSearchHeight", 3, 1, 5);


        builder.pop();
    }

    /**
     * Returns true if ancient books are required, false if the disabled option is true.
     * @return
     */
    public static boolean areAncientBooksRequired() {
        return !ServerConfig.CONFIG.disableAncientBookRequirement.get();
    }

    public static int getBookshelfSearchHeight() {
        return ServerConfig.CONFIG.bookshelfSearchHeight.get();
    }
}
