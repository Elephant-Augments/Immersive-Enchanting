package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for working with enchantments, including conversions between
 * identifiers, holders, and stored enchantment data on items.
 *
 * <p>Provides helper methods for:
 * <ul>
 *     <li>Converting between {@link Identifier} and {@link Holder}</li>
 *     <li>Reading and writing stored enchantments on item stacks</li>
 *     <li>Generating formatted enchantment display names</li>
 * </ul>
 */
public class EnchantmentUtil {

    /**
     * Resolves an enchantment identifier into a {@link Holder}.
     *
     * @param id     the enchantment identifier
     * @param access the registry access used to look up the enchantment
     * @return the corresponding {@link Holder<Enchantment>}
     * @throws java.util.NoSuchElementException if the enchantment is not present
     */
    public static Holder<Enchantment> toHolder(Identifier id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(id).orElseThrow();
    }

    /**
     * Converts an enchantment holder into its identifier.
     *
     * @param enchantmentHolder the enchantment holder
     * @return the identifier of the enchantment
     * @throws java.util.NoSuchElementException if the holder has no registry key
     */
    public static Identifier toId(Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.unwrapKey().orElseThrow().identifier();
    }

    /**
     * Stores a single enchantment on an "ancient book" item stack.
     *
     * <p>This method is intended to run on the server. However, it also supports
     * {@code null} levels to allow usage in contexts where no {@link Level} is
     * available (such as creative tab population).</p>
     *
     * <p>If a non-null level is provided, this method will only execute on the
     * server side. Client-side calls with a valid level are ignored.</p>
     *
     * @param ancientBook        the item stack to modify
     * @param enchantmentHolder  the enchantment to store
     * @param level              the level context, or {@code null} when unavailable
     */
    public static void setStoredEnchantment(ItemStack ancientBook, Holder<Enchantment> enchantmentHolder, @Nullable Level level) {
        if(level == null || !level.isClientSide()) {
            ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            itemEnchantments.set(enchantmentHolder, 1);
            ancientBook.set(DataComponents.STORED_ENCHANTMENTS, itemEnchantments.toImmutable());
        }
    }

    /**
     * Retrieves the stored enchantment identifier from an "ancient book".
     *
     * <p>If multiple enchantments are present, only the first is returned.</p>
     *
     * @param ancientBook the item stack to read from
     * @return the stored enchantment holder, or {@code null} if none is present
     */
    public static @Nullable Holder<Enchantment> getStoredEnchantment(ItemStack ancientBook) {
        ItemEnchantments itemEnchantments = ancientBook.get(DataComponents.STORED_ENCHANTMENTS);
        if(itemEnchantments == null) return null;

        List<Holder<Enchantment>> enchantments = itemEnchantments.keySet().stream().toList();
        if(enchantments.isEmpty()) return null;

        return enchantments.getFirst();
    }

    public static List<Holder<Enchantment>> sortByName(List<Holder<Enchantment>> enchantmentHolders) {
        enchantmentHolders.sort(Comparator.comparing(enchantmentHolder -> enchantmentHolder.value().description().getString()));
         return enchantmentHolders;
    }


}
