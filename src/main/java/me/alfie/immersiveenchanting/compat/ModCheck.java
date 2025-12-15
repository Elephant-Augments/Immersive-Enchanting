package me.alfie.immersiveenchanting.compat;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Separate class to make sure we're not loading any unnecessary classes when mixins are being initialized<br>
 * Example usage:{@code ModCheck.Mod.MODNAME.isLoaded()}
 */
public class ModCheck {
    private static final Map<String, List<String>> ALIAS = Map.of();
    private static final Map<String, Boolean> MODS = new HashMap<>();

    /**
     * Check if a mod is loaded and cache it in ModCheck.MODS
     *
     * @param modid
     * @return
     */
    private static boolean isModLoaded(final String modid) {
        return MODS.computeIfAbsent(modid, key -> {
            if (check(key)) {
                return true;
            }

            for (String alias : ALIAS.getOrDefault(key, List.of())) {
                if (check(alias)) {
                    return true;
                }
            }

            return false;
        });
    }

    /**
     * Query the mod loader to check if a mod of "modid" is present.
     *
     * @param modid
     * @return
     */
    private static boolean check(final String modid) {
        ModList modList = ModList.get();

        if (modList != null && modList.isLoaded(modid)) {
            return true;
        }

        return LoadingModList.get().getModFileById(modid) != null;
    }

    /**
     * Mods that ImmersiveEnchanting recognises and has compatibility with.<br>
     * Mod must be defined in this enum to call {@code #isLoaded()}<br>
     * Note: Ensure mod is added to dependencies in build.gradle
     */
    public enum Mod {
        //Example: ENCHANT_LIMITER("enchant_limiter");
        ; //No definitions yet for Forge 1.20.1

        private final String modid;

        Mod(final String modid) {
            this.modid = modid;
        }

        public boolean isLoaded() {
            return ModCheck.isModLoaded(modid);
        }
    }
}