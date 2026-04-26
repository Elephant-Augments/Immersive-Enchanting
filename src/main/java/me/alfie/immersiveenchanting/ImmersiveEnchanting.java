package me.alfie.immersiveenchanting;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.alfie.immersiveenchanting.block.ModBlocks;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.event.ModEvents;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.loot.ModGlobalLootModifiers;
import me.alfie.immersiveenchanting.sound.ModSounds;
import me.alfie.immersiveenchanting.structure.ModStructureProcessors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.slf4j.Logger;

import java.util.List;

@Mod(ImmersiveEnchanting.MODID)
public class ImmersiveEnchanting {
    public static final String MODID = "immersiveenchanting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImmersiveEnchanting(IEventBus modEventBus, ModContainer modContainer) {
        ModEvents.register(modEventBus);
        ModMenus.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModGlobalLootModifiers.register(modEventBus);
        ModStructureProcessors.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC);
    }

    public static Component styleWithAltFont(Component component) {
        ResourceLocation fontStyle = ResourceLocation.withDefaultNamespace("alt");
        return component.copy().withStyle(Style.EMPTY.withFont(fontStyle));
    }

    /**
     * OLDER VERSIONS ONLY (1.21.1)
     * Villager trades are data driven in newer versions.
     */
    public static void disableEnchantedBookVillagerTrades(VillagerTradesEvent event) {
        if (ServerConfig.isAllowEnchantedBookTrades()) return;

        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (int level : trades.keySet()) {
                List<VillagerTrades.ItemListing> tradeList = trades.get(level);
                if (tradeList == null) continue;

                tradeList.removeIf(
                        listing -> listing instanceof VillagerTrades.EnchantBookForEmeralds);
            }
        }
    }
}
