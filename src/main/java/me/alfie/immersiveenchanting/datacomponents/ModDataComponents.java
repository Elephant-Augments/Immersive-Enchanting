package me.alfie.immersiveenchanting.datacomponents;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ImmersiveEnchanting.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentDataComponent>> ENCHANTMENT = DATA_COMPONENT_TYPES.registerComponentType(
            "no_network",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(EnchantmentDataComponent.BASIC_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(EnchantmentDataComponent.BASIC_STREAM_CODEC)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
