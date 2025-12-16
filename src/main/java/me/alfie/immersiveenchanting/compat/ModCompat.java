package me.alfie.immersiveenchanting.compat;

import net.darkhax.enchdesc.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ModCompat {

    public static DescriptionManager descriptionManager;

    /**
     * Lazy initialise ench desc config.
     */
    private static void loadEnchDescManager() {
        if(ModCheck.Mod.ENCHANTMENT_DESCRIPTIONS.isLoaded()
        && descriptionManager == null) {
            Path configPath = FMLPaths.CONFIGDIR.get();
            ConfigSchema config = ConfigSchema.load(configPath.resolve(Constants.MOD_ID + ".json").toFile());
            descriptionManager = new DescriptionManager(config);
        }
    }

    /**
     * Get EnchDesc for Forge 1.20.1
     * @return
     */
    public static Component getEnchantmentDescription(Enchantment enchantment) {
        if(ModCheck.Mod.ENCHANTMENT_DESCRIPTIONS.isLoaded()) {
            loadEnchDescManager();
            return descriptionManager.get(enchantment);
        }
        return null;
    }

}
