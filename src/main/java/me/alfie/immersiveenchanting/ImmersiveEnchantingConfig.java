package me.alfie.immersiveenchanting;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ImmersiveEnchantingConfig {

    public static final ImmersiveEnchantingConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    // Store the config properties as public finals
    public final ForgeConfigSpec.ConfigValue<Boolean> disableAncientBookRequirement;
    public final ForgeConfigSpec.ConfigValue<Integer> bookshelfSearchHeight;



    static {
        Pair<ImmersiveEnchantingConfig, ForgeConfigSpec> pair =
                new ForgeConfigSpec.Builder().configure(ImmersiveEnchantingConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // Constructor takes only the builder
    public ImmersiveEnchantingConfig(ForgeConfigSpec.Builder builder) {
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
        return !ImmersiveEnchantingConfig.CONFIG.disableAncientBookRequirement.get();
    }

    public static int getBookshelfSearchHeight() {
        return ImmersiveEnchantingConfig.CONFIG.bookshelfSearchHeight.get();
    }
}