package me.alfie.immersiveenchanting.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    // Store the config properties as public finals
    public final ModConfigSpec.ConfigValue<Boolean> showAddedByTooltip;



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
                .define("showAddedByTooltip", true);


        builder.pop();
    }

    /**
     * Returns true if ancient books are required, false if the disabled option is true.
     * @return
     */
    public static boolean isShowAddedByTooltipEnabled() {
        return !ClientConfig.CONFIG.showAddedByTooltip.get();
    }
}
