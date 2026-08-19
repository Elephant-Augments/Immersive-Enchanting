package me.alfie.immersiveenchanting.compat.stylisheffects;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Optional compatibility with Stylish Effects.
 *
 * <p>Adds the enchanting table menu to Stylish Effects' inventory
 * {@code menu_blacklist} so effect widgets are not drawn over the screen.</p>
 */
public final class StylishEffectsCompat {

    private static final String STYLISH_EFFECTS_ID = "stylisheffects";
    private static final String MENU_ID = ImmersiveEnchanting.MODID + ":enchanting_table_menu";

    private StylishEffectsCompat() {}

    public static void register() {
        applyBlacklist();
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof EnchantingTableScreen) {
            applyBlacklist();
        }
    }

    private static void applyBlacklist() {
        if (!ModList.get().isLoaded(STYLISH_EFFECTS_ID)) {
            return;
        }

        try {
            Class<?> stylishEffects = Class.forName("fuzs.stylisheffects.StylishEffects");
            Object configHolder = stylishEffects.getField("CONFIG").get(null);
            Class<?> clientConfigClass = Class.forName("fuzs.stylisheffects.config.ClientConfig");
            Object clientConfig = configHolder.getClass()
                    .getMethod("get", Class.class)
                    .invoke(configHolder, clientConfigClass);
            Object inventoryRenderer = clientConfigClass.getMethod("inventoryRenderer").invoke(clientConfig);

            Field rawField = inventoryRenderer.getClass().getDeclaredField("menuBlacklistRaw");
            rawField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> rawList = (List<Object>) rawField.get(inventoryRenderer);
            if (rawList == null) {
                ImmersiveEnchanting.LOGGER.warn("Stylish Effects menu blacklist unavailable; skipping compat");
                return;
            }
            if (!rawList.contains(MENU_ID)) {
                rawList.add(MENU_ID);
            }
            inventoryRenderer.getClass().getMethod("afterConfigReload").invoke(inventoryRenderer);
        } catch (ReflectiveOperationException exception) {
            ImmersiveEnchanting.LOGGER.warn("Failed to register Stylish Effects compatibility", exception);
        }
    }
}
