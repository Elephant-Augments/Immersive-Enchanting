package me.alfie.immersiveenchanting.lootmodifier;

import com.mojang.serialization.MapCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveEnchanting.MODID);

    public static final Supplier<MapCodec<AncientBookLootModifier>> ANCIENT_BOOK_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("ancient_book_loot_modifier", () -> AncientBookLootModifier.CODEC);

    public static final Supplier<MapCodec<RemoveEnchantedBooksModifier>> REMOVE_ENCHANTED_BOOKS_MODIFIER =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("remove_enchanted_books", () -> RemoveEnchantedBooksModifier.CODEC);

    public static void register(IEventBus eventBus) {
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}
