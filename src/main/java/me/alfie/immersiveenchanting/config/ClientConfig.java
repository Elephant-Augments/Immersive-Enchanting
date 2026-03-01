package me.alfie.immersiveenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    // Store the config properties as public finals
    public final ModConfigSpec.ConfigValue<Boolean> showAddedByTooltip;

    public final ModConfigSpec.ConfigValue<Integer> itemCarouselSpeed;

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
        showAddedByTooltip = builder
                .comment("Display which mod adds the enchantment on ancient books.")
                .translation("immersiveenchanting.config.show_added_by_tooltip")
                .define("showAddedByTooltip", false);

        itemCarouselSpeed = builder
                .comment("How fast items will switch in the enchanting table tooltip (in milliseconds).")
                .translation("immersiveenchanting.config.item_carousel_speed")
                .define("itemCarouselSpeed", 700);


        builder.pop();
    }

    public static boolean isShowAddedByTooltipEnabled() {
        return ClientConfig.CONFIG.showAddedByTooltip.get();
    }

    public static int getItemCarouselSpeed() {
        return ClientConfig.CONFIG.itemCarouselSpeed.get();
    }
}
