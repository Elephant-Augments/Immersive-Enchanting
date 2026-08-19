package me.alfie.immersiveenchanting.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InjectAncientBookLootModifier extends LootModifier {

    public static final MapCodec<InjectAncientBookLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    LootModifier.codecStart(instance).and(
                            instance.group(
                                    Codec.STRING.fieldOf("mode")
                                            .forGetter(e -> e.mode),

                                    Enchantment.CODEC
                                            .listOf()
                                            .optionalFieldOf("enchantments")
                                            .forGetter(e -> Optional.ofNullable(e.enchantments)),

                                    Codec.INT.fieldOf("min_rolls")
                                            .forGetter(e -> e.minRolls),

                                    Codec.INT.fieldOf("max_rolls").
                                            forGetter(e -> e.maxRolls),

                                    Codec.STRING.optionalFieldOf("pool")
                                            .forGetter(e -> Optional.of(e.pool))
                            )
                    ).apply(instance, InjectAncientBookLootModifier::new)
            );

    @Override public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }


    private static final String POOL_ALL = "all";
    private static final String POOL_THEMED = "themed";
    private static final Set<ResourceLocation> ALL_TABLES_BONUS_CHESTS = Set.of(
            ResourceLocation.parse("minecraft:chests/end_city_treasure"),
            ResourceLocation.parse("minecraft:chests/stronghold_library")
    );

    private final String mode;
    private final List<Holder<Enchantment>> enchantments;
    private final int minRolls;
    private final int maxRolls;
    private final String pool;

    public enum Mode {
        RANDOMLY_ENCHANT_FROM_ENABLED("random_enchantment_from_enabled"),
        RANDOMLY_ENCHANT_FROM_ENABLED_EXCEPT("random_enchantment_from_enabled_except"),
        RANDOMLY_ENCHANT_FROM_LIST("random_enchantment_from_list");

        private String string;
        Mode(String string) {
            this.string = string;
        }

        public String getString() {
            return this.string;
        }
    }

    protected InjectAncientBookLootModifier(LootItemCondition[] conditionsIn,
                                            String mode,
                                            Optional<List<Holder<Enchantment>>> enchantments,
                                            int minRolls,
                                            int maxRolls,
                                            Optional<String> pool) {
        super(conditionsIn);
        this.mode = mode;
        this.enchantments = enchantments.orElse(List.of());
        this.minRolls = minRolls;
        this.maxRolls = maxRolls;
        this.pool = pool.orElse(
                Mode.RANDOMLY_ENCHANT_FROM_LIST.getString().equals(mode) ? POOL_THEMED : POOL_ALL
        );
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (context.getRandom().nextDouble() >= resolveChance(context)) {
            return generatedLoot;
        }

        int rolls = context.getRandom().nextIntBetweenInclusive(minRolls, maxRolls);
        for (int i = 0; i < rolls; i++) {
            ItemStack ancientBook = new ItemStack(ModItems.ANCIENT_BOOK.get());

            if(mode.equals(Mode.RANDOMLY_ENCHANT_FROM_ENABLED.getString())) {
                EnchantmentUtil.setStoredEnchantment(ancientBook,
                        CostRegistry.server().getRandomEnchantment(context.getRandom()));

            } else if(mode.equals(Mode.RANDOMLY_ENCHANT_FROM_ENABLED_EXCEPT.getString())) {
                List<Holder<Enchantment>> applicableEnchantments = CostRegistry.server().getAllEnabledEnchantmentHolders();
                applicableEnchantments.removeAll(enchantments);
                int randomIndex = context.getRandom().nextInt(applicableEnchantments.size());

                EnchantmentUtil.setStoredEnchantment(ancientBook,
                        applicableEnchantments.get(randomIndex));

            } else if (mode.equals(Mode.RANDOMLY_ENCHANT_FROM_LIST.getString())) {
                int randomIndex = context.getRandom().nextInt(enchantments.size());

                EnchantmentUtil.setStoredEnchantment(ancientBook,
                        enchantments.get(randomIndex));
            }

            generatedLoot.add(ancientBook);
        }


        return generatedLoot;
    }

    private double resolveChance(LootContext context) {
        if (POOL_THEMED.equals(pool)) {
            return ServerConfig.getThemedTablesChance();
        }
        double chance = ServerConfig.getAllTablesChance();
        ResourceLocation tableId = context.getQueriedLootTableId();
        if (tableId != null && ALL_TABLES_BONUS_CHESTS.contains(tableId)) {
            chance = Math.min(1.0d, chance * 5.0d);
        }
        return chance;
    }

}
