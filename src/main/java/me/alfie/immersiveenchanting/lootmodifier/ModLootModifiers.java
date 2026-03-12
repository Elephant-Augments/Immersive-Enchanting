package me.alfie.immersiveenchanting.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_REGISTER =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    ImmersiveEnchanting.MODID);

    public static final RegistryObject<Codec<AncientBookLootModifier>> ANCIENT_BOOK_LOOT_MODIFIER
            = GLOBAL_LOOT_MODIFIER_REGISTER.register("ancient_book_loot_modifier", () -> AncientBookLootModifier.CODEC);

    public static final RegistryObject<Codec<RemoveEnchantedBooksModifier>> REMOVE_ENCHANTED_BOOKS_MODIFIER =
            GLOBAL_LOOT_MODIFIER_REGISTER.register("remove_enchanted_books", () -> RemoveEnchantedBooksModifier.CODEC);

    public static void register(IEventBus eventBus) {
        GLOBAL_LOOT_MODIFIER_REGISTER.register(eventBus);
    }

}
