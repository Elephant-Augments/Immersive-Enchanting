package me.alfie.immersiveenchanting;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import me.alfie.immersiveenchanting.block.ModBlocks;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostDatapackHandler;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.lootmodifier.ModLootModifiers;
import me.alfie.immersiveenchanting.networking.packets.*;
import me.alfie.immersiveenchanting.sound.ModSounds;
import me.alfie.immersiveenchanting.structure.ModStructureProcessors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.util.Optional;

@Mod(ImmersiveEnchanting.MODID)
public class ImmersiveEnchanting {
    public static final String MODID = "immersiveenchanting";
    public static final Logger LOGGER = LogUtils.getLogger();

    protected static final EnchantmentCostDatapackHandler ENCHANTMENT_COST_DATAPACK_HANDLER = new EnchantmentCostDatapackHandler(
            new Gson(), EnchantmentCostDatapackHandler.DIRECTORY);

    public ImmersiveEnchanting(IEventBus modEventBus, ModContainer modContainer) {
        ImmersiveEnchantingEvents events = new ImmersiveEnchantingEvents();
        modEventBus.addListener(events::onClientStart);
        modEventBus.addListener(events::buildCreativeTab);
        modEventBus.addListener(events::onLoadComplete);
        NeoForge.EVENT_BUS.register(events);

        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModStructureProcessors.register(modEventBus);
        ModSounds.register(modEventBus);

        modEventBus.addListener(ModMenus::registerMenuScreens);
        modEventBus.addListener(EnchantItemPacket::register);
        modEventBus.addListener(GetBookshelfContentsPacket::register);
        modEventBus.addListener(UnlockedEnchantmentsPacket::register);
        modEventBus.addListener(EnchantmentCostRegistrySyncPacket::register);
        modEventBus.addListener(UpdateToolSlotPacket::register);
        modEventBus.addListener(TransmuteBookPacket::register);
        modEventBus.addListener(ReplicateBookPacket::register);
        modEventBus.addListener(RemoveEnchantmentPacket::register);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC);
    }

    /**
     * Return the enchantment registry from a RegistryAccess.
     * @param access
     * @return
     */
    public static Registry<Enchantment> getEnchantmentRegistry(RegistryAccess access) {
        return access.registryOrThrow(Registries.ENCHANTMENT);
    }

    public static HolderLookup<Enchantment> getEnchantmentHolderLookup(RegistryAccess access) {
        return getEnchantmentRegistry(access).asLookup();
    }

    /**
     * Get an enchantment holder using a holder lookup.
     * @param lookup
     * @param enchantmentResourceId
     * @return
     */
    private static Optional<Holder.Reference<Enchantment>> getEnchantmentHolder(HolderLookup<Enchantment> lookup, String enchantmentResourceId) {
        try {
            ResourceLocation location = ResourceLocation.tryParse(enchantmentResourceId);
            if (location == null) return Optional.empty();

            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, location);
            return lookup.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Automatically get an enchantment holder using RegistryAccess.
     * @param access
     * @param enchantment
     * @return
     */
    public static Optional<Holder.Reference<Enchantment>> getEnchantmentHolder(RegistryAccess access, ResourceKey<Enchantment> enchantment) {
        return access.registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment);
    }

    public static ResourceLocation getEnchantmentHolderRL(Holder<Enchantment> enchantmentHolder) {
        return ResourceLocation.parse(enchantmentHolder.getRegisteredName());
    }

}
