package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
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
    public static Holder<Enchantment> toHolder(Identifier id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(id).orElseThrow();
    }

    /**
     * Converts a registry holder into its identifier.
     *
     * @param enchantmentHolder enchantment holder
     * @return identifier of the enchantment
     * @throws java.util.NoSuchElementException if the holder has no registry key
     */
    public static Identifier toId(Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.unwrapKey().orElseThrow().identifier();
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
                                     IPayloadContext context) {
        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        if(!isNextLevel(stackToEnchant, enchantmentHolder, level)) return false;
        if(!stackToEnchant.supportsEnchantment(enchantmentHolder)) return false;
        if(!isEnchantmentAvailableInBookshelves(enchantmentHolder, menu, context)) return false;

        if(context.player().hasInfiniteMaterials()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), toId(enchantmentHolder), level, context.player(), CostRegistry.server())) return false;

        return true;
    }

    /**
     * Checks whether a transmutation operation is allowed.
     *
     * @param menu enchanting menu
     * @param newEnchantment enchantment to apply
     * @param context player/network context
     * @return true if transmutation is allowed
     */
    public static boolean canTransmute(EnchantingTableMenu menu, Holder<Enchantment> newEnchantment, IPayloadContext context) {
        if(!isEnchantmentAvailableInBookshelves(newEnchantment, menu, context)) return false;
        if(EnchantmentUtil.isReplicated(menu.getToolSlot().getItem())) return false;

        if(context.player().hasInfiniteMaterials()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), CostRegistry.TRANSMUTE, 1, context.player(), CostRegistry.server())) return false;

        return true;
    }

    /**
     * Checks whether replication is allowed.
     *
     * @param menu enchanting menu
     * @param context player/network context
     * @return true if replication is allowed
     */
    public static boolean canReplicate(EnchantingTableMenu menu, IPayloadContext context) {
        if(context.player().hasInfiniteMaterials()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), CostRegistry.REPLICATE, 1, context.player(), CostRegistry.server())) return false;

        return true;
    }

    /**
     * Validates whether an enchantment is available from surrounding bookshelves.
     *
     * @param enchantmentHolder enchantment to check
     * @param menu enchanting menu
     * @param context player context
     * @return true if available
     */
    private static boolean isEnchantmentAvailableInBookshelves(Holder<Enchantment> enchantmentHolder, EnchantingTableMenu menu, IPayloadContext context) {
        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), context.player().level());
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

    /**
     * Checks if a valid cost exists for the given enchantment.
     *
     * @param costStack item used for cost validation
     * @param enchantmentId enchantment being applied
     * @param level target position
     * @param player player performing the action
     * @param costRegistry cost registry used for lookup
     * @return true if a valid cost exists
     */
    public static boolean hasValidCost(ItemStack costStack,
                                       Identifier enchantmentId, int level,
                                       Player player, CostRegistry costRegistry) {
        return findValidCost(costStack, enchantmentId, level, player, costRegistry) != null;
    }

    /**
     * Checks if valid enchanting fuel exists for the given position.
     *
     * @param fuelStack fuel item stack
     * @param level target position
     * @param costRegistry cost registry used for lookup
     * @return true if valid fuel exists
     */
    public static boolean hasValidEnchantingFuel(ItemStack fuelStack, int level, CostRegistry costRegistry) {
        return findValidEnchantingFuel(fuelStack, level, costRegistry) != null;
    }

    /**
     * Checks whether both cost and fuel requirements are satisfied.
     *
     * @param costStack item used for cost
     * @param fuelStack item used for fuel
     * @param enchantmentId enchantment being applied
     * @param level target position
     * @param player player performing the action
     * @param costRegistry cost registry used for lookup
     * @return true if both cost and fuel are valid
     */
    public static boolean hasValidCostAndFuel(ItemStack costStack, ItemStack fuelStack,
                                              Identifier enchantmentId, int level,
                                              Player player, CostRegistry costRegistry) {
        return hasValidCost(costStack, enchantmentId, level, player, costRegistry)
                && hasValidEnchantingFuel(fuelStack, level, costRegistry);
    }

    /**
     * Finds a valid cost entry for the given enchantment and position.
     *
     * @param costStack item used for validation
     * @param enchantmentId enchantment being applied
     * @param level target position
     * @param player player performing the action
     * @param costRegistry cost registry used for lookup
     * @return matching cost, or null if none found
     */
    private static Cost findValidCost(ItemStack costStack, Identifier enchantmentId, int level,
                                      Player player, CostRegistry costRegistry) {
        List<Cost> validCosts = costRegistry.get(enchantmentId).levelCosts().getLevel(level).costs();

        for(Cost validCost : validCosts) {
            if(!playerHasEnoughLevels(player, validCost)) continue;

            //Check all item stacks in validCost
            for(ItemStack validCostStack : validCost.getItemStacks()) {
                if(isCostStackValid(costStack, validCostStack)) {
                    return validCost;
                }
            }
        }

        return null;
    }

    /**
     * Finds a valid fuel cost for the given position.
     *
     * @param fuelStack fuel item stack
     * @param level target position
     * @param costRegistry cost registry used for lookup
     * @return matching fuel cost, or null if none found
     */
    private static Cost findValidEnchantingFuel(ItemStack fuelStack, int level, CostRegistry costRegistry) {
        List<Cost> validFuels = costRegistry.get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(level).costs();

        for(Cost validFuel : validFuels) {
            for(ItemStack validFuelStack : validFuel.getItemStacks()) {
                if(isCostStackValid(fuelStack, validFuelStack)) return validFuel;
            }
        }

        return null;
    }

    /**
     * Checks if two item stacks match in type and quantity.
     *
     * @param costStack actual stack
     * @param validCostStack required stack
     * @return true if stack is valid
     */
    private static boolean isCostStackValid(ItemStack costStack, ItemStack validCostStack) {
        return costStack.getItem().equals(validCostStack.getItem()) && costStack.count() >= validCostStack.count();
    }

    /**
     * Checks whether the player has enough XP levels for a cost.
     *
     * @param player player to check
     * @param validCost cost requirement
     * @return true if player has enough levels
     */
    private static boolean playerHasEnoughLevels(Player player, Cost validCost) {
        return player.experienceLevel >= validCost.xpLevels();
    }

    /**
     * Deducts a validated enchantment cost and fuel from the menu.
     *
     * <p>Throws if no valid cost is found (should be pre-validated before calling).
     *
     * @param menu enchanting menu
     * @param enchantmentId enchantment being applied
     * @param level target position
     * @param player player performing the action
     * @param costRegistry cost registry used for lookup
     * @throws IllegalStateException if no valid cost exists
     */
    public static void deductValidCost(EnchantingTableMenu menu,
                                       Identifier enchantmentId, int level,
                                       Player player, CostRegistry costRegistry) {
        if(player.hasInfiniteMaterials()) return;

        ItemStack costStack = menu.getCostSlot().getItem();
        ItemStack fuelStack = menu.getFuelSlot().getItem();
        if(!hasValidCostAndFuel(costStack, fuelStack, enchantmentId, level, player, costRegistry)) throw new IllegalStateException("Cannot deduct cost as there is no valid cost!");

        Cost validCost = findValidCost(costStack, enchantmentId, level, player, costRegistry);
        Cost validFuel = findValidEnchantingFuel(fuelStack, level, costRegistry);

        menu.getCostSlot().getItem().shrink(validCost.itemStackHolder().amount());
        menu.getFuelSlot().getItem().shrink(validFuel.itemStackHolder().amount());

        player.giveExperienceLevels(-validCost.xpLevels());
    }

    /**
     * Retrieves all registered enchantments from a lookup provider.
     *
     * @param lookup registry lookup provider
     * @return list of all enchantments
     */
    public static List<Holder.Reference<Enchantment>> getAllRegisteredEnchantments(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Registries.ENCHANTMENT).listElements().toList();
    }
}
