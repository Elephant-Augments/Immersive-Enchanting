package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.datapack.ModDatapack;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.networking.ModPackets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(ModDatapack::registerServerDatapack);
        modEventBus.addListener(ModDatapack::registerClientDatapack);

        modEventBus.addListener(ModMenus::registerScreens);
        modEventBus.addListener(ModPackets::registerClient);
        modEventBus.addListener(ModPackets::registerServer);
    }

}
