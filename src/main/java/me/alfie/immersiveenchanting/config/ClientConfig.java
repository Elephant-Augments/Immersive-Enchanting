package me.alfie.immersiveenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.ConfigValue<Boolean> SHOW_ADDED_BY_TOOLTIP;
    public final ModConfigSpec.ConfigValue<Integer> ITEM_CAROUSEL_SPEED;
    public final ModConfigSpec.ConfigValue<Boolean> PLAY_NODE_HOVER_SOUNDS;
    public final ModConfigSpec.ConfigValue<Boolean> COLOR_ENCHANTMENT_TOOLTIPS_BY_FILTER;

    static {
        Pair<ClientConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ClientConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    // Constructor takes only the builder
    public ClientConfig(ModConfigSpec.Builder builder) {
        builder.push("general"); // optional grouping

        // Define the config value here
        SHOW_ADDED_BY_TOOLTIP = builder
                .comment("Display which mod adds the enchantment on ancient books.")
                .translation("immersiveenchanting.config.show_added_by_tooltip")
                .define("showAddedByTooltip", false);

        ITEM_CAROUSEL_SPEED = builder
                .comment("How fast items will switch in the enchanting table tooltip (in milliseconds).")
                .translation("immersiveenchanting.config.item_carousel_speed")
                .define("itemCarouselSpeed", 700);

        PLAY_NODE_HOVER_SOUNDS = builder
                .comment("Play node hover sounds in the enchanting table GUI.")
                .translation("immersiveenchanting.config.play_node_hover_sounds")
                .define("playNodeHoverSounds", true);

        COLOR_ENCHANTMENT_TOOLTIPS_BY_FILTER = builder
                .comment("Color enchantment titles by their isolated filter branch on items and in the enchanting GUI.")
                .translation("immersiveenchanting.config.color_enchantment_tooltips_by_filter")
                .define("colorEnchantmentTooltipsByFilter", true);

        builder.pop();
    }

    public static boolean isShowAddedByTooltipEnabled() {
        return ClientConfig.CONFIG.SHOW_ADDED_BY_TOOLTIP.get();
    }

    public static boolean areNodeHoverSoundsEnabled() {return ClientConfig.CONFIG.PLAY_NODE_HOVER_SOUNDS.get();}

    public static boolean isColorEnchantmentTooltipsByFilterEnabled() {
        return ClientConfig.CONFIG.COLOR_ENCHANTMENT_TOOLTIPS_BY_FILTER.get();
    }

    public static int getItemCarouselSpeed() {
        return ClientConfig.CONFIG.ITEM_CAROUSEL_SPEED.get();
    }
}
