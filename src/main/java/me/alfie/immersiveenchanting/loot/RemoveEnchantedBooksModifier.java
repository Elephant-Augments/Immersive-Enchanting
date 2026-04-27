package me.alfie.immersiveenchanting.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.alfie.immersiveenchanting.config.ServerConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;


public class RemoveEnchantedBooksModifier extends LootModifier {

    public static final Codec<RemoveEnchantedBooksModifier> CODEC = RecordCodecBuilder
            .create(inst ->
                    LootModifier.codecStart(inst).apply(inst, RemoveEnchantedBooksModifier::new));


    public RemoveEnchantedBooksModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }


    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {

        if(!ServerConfig.isAllowEnchantedBookLootTables())
            generatedLoot.removeIf(stack -> stack.is(Items.ENCHANTED_BOOK));

        return generatedLoot;
    }
}
