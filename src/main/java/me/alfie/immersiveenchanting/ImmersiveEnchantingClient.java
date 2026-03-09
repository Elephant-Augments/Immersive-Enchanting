package me.alfie.immersiveenchanting;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ImmersiveEnchanting.MODID, dist = Dist.CLIENT)
public class ImmersiveEnchantingClient {

    public ImmersiveEnchantingClient(ModContainer container) {
        //NeoForge's ConfigurationScreen to display mod configs
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}