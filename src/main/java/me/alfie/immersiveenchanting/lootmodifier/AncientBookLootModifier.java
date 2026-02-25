package me.alfie.immersiveenchanting.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.legacy.LevelCost;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;


public class AncientBookLootModifier extends LootModifier {


    public static final MapCodec<AncientBookLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            // LootModifier#codecStart adds the conditions field.
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.INT.fieldOf("count").forGetter(e -> e.count),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(e -> e.item),
                    Codec.FLOAT.fieldOf("chance").forGetter(e -> e.chance)
            )).apply(inst, AncientBookLootModifier::new)
    );

    // Our extra properties.
    private final int count;
    private final Item item;
    private final float chance;

    // First constructor parameter is the list of conditions. The rest is our extra properties.
    public AncientBookLootModifier(LootItemCondition[] conditions, int count, Item item, float chance) {
        super(conditions);
        this.count = count;
        this.item = item;
        this.chance = chance;
    }

    // Return our codec here.
    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    /**
     * Apply loot modifier.
     * Fires server-side.
     * @param generatedLoot
     * @param context
     * @return
     */
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < chance) { // chance from JSON
            ItemStack lootItem = new ItemStack(item, count);

            if (lootItem.getItem() == ModItems.ANCIENT_BOOK.get()) {
                Holder<Enchantment> randomEnchantment = getRandomEnchantment(context.getLevel(), context.getRandom());
                AncientBook.setStoredEnchantment(lootItem, randomEnchantment);
            }
            generatedLoot.add(lootItem);
        }
        return generatedLoot;
    }

    public static List<Holder.Reference<Enchantment>> getAllEnchantments(Level level) {
        // Get all available types
        RegistryAccess registryAccess = level.registryAccess();
        HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder.Reference<Enchantment>> allEnchantments = lookup.listElements().toList();

        // Filter out MENDING if disabled
        List<Holder.Reference<Enchantment>> filteredEnchantments = allEnchantments.stream()
                .filter(enchantment -> {
                    ResourceLocation keyLocation = enchantment.key().location();

                    // Remove disabled enchantments, such as mending
                    //If enchantment has DO_NOT_INCLUDE tag. (Empty json)

                    //BUG-FIX! Cannot access getClientRegistry() here, as this method runs server-side.
                    //Use getServerRegistry()
                    if (EnchantmentCostRegistry.getServerRegistry().getCostRegistry().containsKey(enchantment.key())) {
                        if (EnchantmentCostRegistry.getServerRegistry().getCostRegistry()
                                .get(enchantment.key())
                                .getLevel(-1).item().equals(LevelCost.DO_NOT_INCLUDE)) {
                            return false;
                        }
                    }

                    // Skip cursed enchantments
                    if (enchantment.is(EnchantmentTags.CURSE)) {
                        return false;
                    }
                    return true; // include everything else
                })
                .toList();
        return filteredEnchantments;
    }

    /**
     * Get a list of all the available types of enchantment (based on the data pack) and return a random element.
     * @return
     */
    public static Holder<Enchantment> getRandomEnchantment(Level level, RandomSource randomSource) {
        List<Holder.Reference<Enchantment>> filteredEnchantments = getAllEnchantments(level);

        // Pick a random enchantment type from the filtered list
        if (!filteredEnchantments.isEmpty()) {
            Holder.Reference<Enchantment> randomEnchantment = filteredEnchantments.get(randomSource.nextInt(filteredEnchantments.size()));
            return randomEnchantment;
        }
        return null;
    }
}
