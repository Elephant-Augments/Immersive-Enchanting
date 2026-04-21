package me.alfie.immersiveenchanting.loot;

import com.mojang.serialization.MapCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModGlobalLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveEnchanting.MODID);

    public static final Supplier<MapCodec<InjectAncientBookLootModifier>> ANCIENT_BOOK_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIERS.register("ancient_book_loot_modifier", () -> InjectAncientBookLootModifier.CODEC);

    public static void register(IEventBus eventBus) {
        GLOBAL_LOOT_MODIFIERS.register(eventBus);
    }
}
