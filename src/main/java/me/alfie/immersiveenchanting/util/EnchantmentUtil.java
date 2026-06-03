package me.alfie.immersiveenchanting.util;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostHolder;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility methods for working with enchantments, item data, and cost validation.
 *
 * <p>Handles common tasks such as:
 * <ul>
 *     <li>Converting between {@link Identifier} and {@link Holder}</li>
 *     <li>Reading and writing stored enchantments on item stacks</li>
 *     <li>Validating enchantment application rules</li>
 *     <li>Resolving and deducting enchantment costs</li>
 * </ul>
 *
 * <p>Primarily used by enchanting systems (e.g. {@link EnchantingTableMenu}).
 *
 * <p>Most methods assume valid inputs and correct execution context.
 */
public class EnchantmentUtil {

    /**
     * Converts an enchantment identifier into a registry holder.
     *
     * @param id enchantment identifier
     * @param access registry access used for lookup
     * @return enchantment holder
     * @throws java.util.NoSuchElementException if the enchantment is not found
     */
    public static Holder<Enchantment> toHolder(ResourceId id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(id.mc()).orElseThrow();
    }

    public static Holder<Enchantment> toHolder(ResourceKey<Enchantment> enchantmentKey, RegistryAccess access) {
        return toHolder(ResourceId.parse(enchantmentKey.identifier().toString()), access);
    }

    /**
     * Converts a registry holder into its identifier.
     *
     * @param enchantmentHolder enchantment holder
     * @return identifier of the enchantment
     * @throws java.util.NoSuchElementException if the holder has no registry key
     */
    public static ResourceId toId(Holder<Enchantment> enchantmentHolder) {
        return ResourceId.parse(enchantmentHolder.unwrapKey().orElseThrow().identifier().toString());
    }

    public static List<ResourceKey<Enchantment>> toResourceKeys(List<Holder<Enchantment>> enchantmentHolders) {
        List<ResourceKey<Enchantment>> result = new ArrayList<>(enchantmentHolders.size());
        for(Holder<Enchantment> enchantmentHolder : enchantmentHolders) {
            result.add(enchantmentHolder.getKey());
        }
        return result;
    }

    public static List<Holder<Enchantment>> toHolders(List<ResourceKey<Enchantment>> enchantmentKeys, RegistryAccess registryAccess) {
        List<Holder<Enchantment>> result = new ArrayList<>(enchantmentKeys.size());
        for(ResourceKey<Enchantment> enchantmentKey : enchantmentKeys) {
            result.add(toHolder(ResourceId.parse(enchantmentKey.identifier().toString()), registryAccess));
        }
        return result;
    }

    /**
     * Stores a single enchantment on an item as stored enchantments.
     *
     * @param ancientBook item stack to modify
     * @param enchantmentHolder enchantment to store
     */
    public static void setStoredEnchantment(ItemStack ancientBook, Holder<Enchantment> enchantmentHolder) {
        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        itemEnchantments.set(enchantmentHolder, 1);
        ancientBook.set(DataComponents.STORED_ENCHANTMENTS, itemEnchantments.toImmutable());
    }

    /**
     * Retrieves the first stored enchantment from an item.
     *
     * @param ancientBook item stack to read from
     * @return stored enchantment holder, or {@code null} if none exists
     */
    public static @Nullable Holder<Enchantment> getStoredEnchantment(ItemStack ancientBook) {
        ItemEnchantments itemEnchantments = ancientBook.get(DataComponents.STORED_ENCHANTMENTS);
        if(itemEnchantments == null) return null;

        List<Holder<Enchantment>> enchantments = itemEnchantments.keySet().stream().toList();
        if(enchantments.isEmpty()) return null;

        return enchantments.getFirst();
    }

    /**
     * Marks an item as replicated.
     *
     * @param stack item stack to modify
     */
    public static void setReplicated(ItemStack stack) {
        stack.set(ModDataComponents.REPLICATED, new ReplicatedDataComponent(true));
    }

    /**
     * Checks if an item is marked as replicated.
     *
     * @param stack item stack to check
     * @return true if replicated, false otherwise
     */
    public static boolean isReplicated(ItemStack stack) {
        if(!stack.has(ModDataComponents.REPLICATED)) return false;
        return stack.get(ModDataComponents.REPLICATED.get()).isReplicated();
    }

    /**
     * Sorts enchantments alphabetically by display name.
     *
     * @param enchantmentHolders list to sort (modified in place)
     * @return sorted list of enchantments
     */
    public static List<Holder<Enchantment>> sortByName(List<Holder<Enchantment>> enchantmentHolders) {
        enchantmentHolders.sort(Comparator.comparing(enchantmentHolder -> enchantmentHolder.value().description().getString()));
         return enchantmentHolders;
    }

    /**
     * Checks whether an enchantment can be applied in the enchanting table.
     *
     * @param menu enchanting menu
     * @param enchantmentHolder enchantment to apply
     * @param level target position
     * @param context network context / player context
     * @return true if enchantment can be applied
     */
    public static boolean canEnchant(EnchantingTableMenu menu,
                                     Holder<Enchantment> enchantmentHolder, int level,
                                     Player player) {
        if(!CostRegistry.server().isRegistered(enchantmentHolder) ||
            !CostRegistry.server().isRegistered(CostRegistry.ENCHANTING_FUELS)) return false;
        if(!CostRegistry.server().get(enchantmentHolder).enabled()) return false;

        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        if(!isNextLevel(stackToEnchant, enchantmentHolder, level)) return false;
        if(!stackToEnchant.supportsEnchantment(enchantmentHolder)) return false;
        if(!isEnchantmentAvailableInBookshelves(enchantmentHolder, menu, player)) return false;

        return tryConsumeValidCostAndFuel(
                toId(enchantmentHolder), level,
                menu, player);
    }

    /**
     * Checks whether a transmutation operation is allowed.
     *
     * @param menu enchanting menu
     * @param oldEnchantment enchantment to apply
     * @param context player/network context
     * @return true if transmutation is allowed
     */
    public static boolean canTransmute(EnchantingTableMenu menu, Holder<Enchantment> oldEnchantment, Player player) {
        if(!CostRegistry.server().isRegistered(CostRegistry.TRANSMUTE)) return false;
        if(!CostRegistry.server().get(CostRegistry.TRANSMUTE).enabled()) return false;
        if(!isEnchantmentAvailableInBookshelves(oldEnchantment, menu, player)) return false;
        if(EnchantmentUtil.isReplicated(menu.getToolSlot().getItem())) return false;

        return tryConsumeValidCostAndFuel(
                CostRegistry.TRANSMUTE, 1,
                menu, player);
    }

    /**
     * Checks whether replication is allowed.
     *
     * @param menu enchanting menu
     * @param context player/network context
     * @return true if replication is allowed
     */
    public static boolean canReplicate(EnchantingTableMenu menu, Player player) {
        if(!CostRegistry.server().isRegistered(CostRegistry.REPLICATE)) return false;
        if(!CostRegistry.server().get(CostRegistry.REPLICATE).enabled()) return false;

        return tryConsumeValidCostAndFuel(
                CostRegistry.REPLICATE, 1,
                menu, player);
    }

    /**
     * Validates whether an enchantment is available from surrounding bookshelves.
     *
     * @param enchantmentHolder enchantment to check
     * @param menu enchanting menu
     * @param context player context
     * @return true if available
     */
    private static boolean isEnchantmentAvailableInBookshelves(Holder<Enchantment> enchantmentHolder, EnchantingTableMenu menu, Player player) {
        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), player.level());
        return availableEnchantments.contains(enchantmentHolder);
    }

    /**
     * Checks if the enchantment is the next valid position.
     *
     * @param stack item being enchanted
     * @param enchantmentHolder enchantment
     * @param level target position
     * @return true if next position
     */
    private static boolean isNextLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder, int level) {
        int equippedLevel = stack.getEnchantmentLevel(enchantmentHolder);
        return level == equippedLevel + 1;
    }

    public static boolean tryConsumeValidCostAndFuel(ResourceId costId,
                                                     int costLevel,
                                                     EnchantingTableMenu menu,
                                                     Player player) {
        if(player.hasInfiniteMaterials()) return true;
        CostHolder costHolder = CostRegistry.server().get(costId).levelCosts().getLevel(costLevel);
        CostHolder fuelHolder = CostRegistry.server().get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(costLevel);

        Cost validCost = null;
        Cost validFuel = null;

        for(Cost cost : costHolder.costs()) {
            validCost = cost.test(menu.getCostSlot().getItem(), player);
        }

        for(Cost cost : fuelHolder.costs()) {
            validFuel = cost.test(menu.getFuelSlot().getItem(), player);
        }

        if(validCost == null || validFuel == null) return false;

        validCost.itemCost().tryConsume(menu.getCostSlot().getItem());
        validFuel.itemCost().tryConsume(menu.getFuelSlot().getItem());
        return true;
    }

    /**
     * Retrieves all registered enchantments from a lookup provider.
     *
     * @param lookup registry lookup provider
     * @return list of all enchantments
     */
    public static List<Holder<Enchantment>> getAllRegisteredEnchantments(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Registries.ENCHANTMENT).listElements().map(holder -> (Holder<Enchantment>) holder).toList();
    }
}
