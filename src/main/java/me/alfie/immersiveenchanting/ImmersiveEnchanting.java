package me.alfie.immersiveenchanting;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.alfie.immersiveenchanting.block.ModBlocks;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.event.ModEvents;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.loot.ModGlobalLootModifiers;
import me.alfie.immersiveenchanting.networking.ModPackets;
import me.alfie.immersiveenchanting.sound.ModSounds;
import me.alfie.immersiveenchanting.structure.ModStructureProcessors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

@Mod(ImmersiveEnchanting.MODID)
public class ImmersiveEnchanting {
    public static final String MODID = "immersiveenchanting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImmersiveEnchanting(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModEvents.register(modEventBus);
        ModMenus.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModGlobalLootModifiers.register(modEventBus);
        ModStructureProcessors.register(modEventBus);
        ModPackets.register();

        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC);
    }

    public static Component styleWithAltFont(Component component) {
        ResourceLocation fontStyle = ResourceLocation.withDefaultNamespace("alt");
        return component.copy().withStyle(Style.EMPTY.withFont(fontStyle));
    }

    /**
     * OLDER VERSIONS ONLY (1.20.1)
     * Villager trades are data driven in newer versions.
     */
    public static void disableEnchantedBookVillagerTrades(VillagerTradesEvent event) {
        if (ServerConfig.isAllowEnchantedBookTrades()) return;

        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (int level : trades.keySet()) {
                List<VillagerTrades.ItemListing> tradeList = trades.get(level);
                if (tradeList == null) continue;

                for (VillagerTrades.ItemListing trade : tradeList) {
                    String className = trade.getClass().getSimpleName();
                    //So hacky but VillagerTrades.EnchantBookForEmeralds is private
                    if ("EnchantBookForEmeralds".equals(className)) {
                        tradeList.remove(trade);
                    }
                }
            }
        }
    }
}
