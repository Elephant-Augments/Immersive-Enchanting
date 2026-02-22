package me.alfie.immersiveenchanting.datacomponent;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ImmersiveEnchanting.MODID);

    @Deprecated
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentDataComponent>> LEGACY_ENCHANTMENT = DATA_COMPONENT_TYPES.registerComponentType(
            "no_network",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(EnchantmentDataComponent.BASIC_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(EnchantmentDataComponent.BASIC_STREAM_CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReplicatedDataComponent>> REPLICATED = DATA_COMPONENT_TYPES.registerComponentType(
            "replicated",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(ReplicatedDataComponent.BASIC_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ReplicatedDataComponent.BASIC_STREAM_CODEC)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}