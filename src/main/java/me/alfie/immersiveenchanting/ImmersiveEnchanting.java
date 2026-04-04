package me.alfie.immersiveenchanting;

import com.mojang.logging.LogUtils;
import me.alfie.immersiveenchanting.datapack.ModDatapack;
import me.alfie.immersiveenchanting.event.ModEvents;
import me.alfie.immersiveenchanting.gui.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ImmersiveEnchanting.MODID)
public class ImmersiveEnchanting {
    public static final String MODID = "immersiveenchanting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImmersiveEnchanting(IEventBus modEventBus, ModContainer modContainer) {
        ModEvents.register(modEventBus);
        ModMenus.register(modEventBus);
    }

    public static Component styleWithAltFont(Component component) {
        FontDescription altFont = new FontDescription.Resource(Identifier.withDefaultNamespace("alt"));
        return component.copy().withStyle(Style.EMPTY.withFont(altFont));
    }
}
