package me.alfie.immersiveenchanting.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class ModKeyMappings {

    public static final KeyMapping MOD_FILTER = new KeyMapping(
            "key.immersiveenchanting.mod_filter",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_TAB,
            "key.categories.immersiveenchanting"
    );

    private ModKeyMappings() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(MOD_FILTER);
    }

    public static Component modFilterTooltip() {
        Component key = Component.literal("[")
                .append(MOD_FILTER.getTranslatedKeyMessage())
                .append("]");
        return Component.translatable("immersiveenchanting.tooltip.hold_mod_filter", key);
    }

    public static boolean isModFilterKey(int keyCode, int scanCode) {
        return MOD_FILTER.matches(keyCode, scanCode);
    }
}
