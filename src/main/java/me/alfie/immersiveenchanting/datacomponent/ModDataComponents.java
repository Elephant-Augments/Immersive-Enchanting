package me.alfie.immersiveenchanting.datacomponent;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ImmersiveEnchanting.MODID);

    public static final DeferredHolder<DataComponentType<?>, @NotNull DataComponentType<ReplicatedDataComponent>> REPLICATED = DATA_COMPONENT_TYPES.registerComponentType(
            "replicated",
            builder -> builder
                    .persistent(ReplicatedDataComponent.CODEC)
                    .networkSynchronized(ReplicatedDataComponent.STREAM_CODEC)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
