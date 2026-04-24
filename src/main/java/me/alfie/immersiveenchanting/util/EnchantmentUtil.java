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
 * Utility class providing helper methods for working with enchantments.
 *
 * <p>This class centralises common logic related to enchantment handling,
 * including:</p>
 * <ul>
 *     <li>Converting between {@link Identifier} and {@link Holder} representations</li>
 *     <li>Reading and writing stored enchantments on item stacks</li>
 *     <li>Sorting and accessing enchantment data</li>
 *     <li>Validating whether an enchantment can be applied</li>
 *     <li>Resolving and deducting enchantment costs</li>
 * </ul>
 *
 * <p>It is primarily used by enchanting-related systems such as custom
 * enchanting tables and item interactions.</p>
 *
 * <p><b>Note:</b> Some methods assume server-side execution and/or prior
 * validation (e.g. cost deduction). Callers are responsible for ensuring
 * correct usage.</p>
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
     */
    public static void setStoredEnchantment(ItemStack ancientBook, Holder<Enchantment> enchantmentHolder) {
        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        itemEnchantments.set(enchantmentHolder, 1);
        ancientBook.set(DataComponents.STORED_ENCHANTMENTS, itemEnchantments.toImmutable());
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

    public static void setReplicated(ItemStack stack) {
        stack.set(ModDataComponents.REPLICATED, new ReplicatedDataComponent(true));
    }

    public static boolean isReplicated(ItemStack stack) {
        if(!stack.has(ModDataComponents.REPLICATED)) return false;
        return stack.get(ModDataComponents.REPLICATED.get()).isReplicated();
    }

    public static List<Holder<Enchantment>> sortByName(List<Holder<Enchantment>> enchantmentHolders) {
        enchantmentHolders.sort(Comparator.comparing(enchantmentHolder -> enchantmentHolder.value().description().getString()));
         return enchantmentHolders;
    }

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

    public static boolean canTransmute(EnchantingTableMenu menu, Holder<Enchantment> newEnchantment, IPayloadContext context) {
        if(!isEnchantmentAvailableInBookshelves(newEnchantment, menu, context)) return false;

        if(context.player().hasInfiniteMaterials()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), CostRegistry.TRANSMUTE, 1, context.player(), CostRegistry.server())) return false;

        return true;
    }

    public static boolean canReplicate(EnchantingTableMenu menu, IPayloadContext context) {
        if(context.player().hasInfiniteMaterials()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), CostRegistry.REPLICATE, 1, context.player(), CostRegistry.server())) return false;

        return true;
    }

    private static boolean isEnchantmentAvailableInBookshelves(Holder<Enchantment> enchantmentHolder, EnchantingTableMenu menu, IPayloadContext context) {
        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), context.player().level());
        return availableEnchantments.contains(enchantmentHolder);
    }

    private static boolean isNextLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder, int level) {
        int equippedLevel = stack.getEnchantmentLevel(enchantmentHolder);
        return level == equippedLevel + 1;
    }

    public static boolean hasValidCost(ItemStack costStack,
                                       Identifier enchantmentId, int level,
                                       Player player, CostRegistry costRegistry) {
        return findValidCost(costStack, enchantmentId, level, player, costRegistry) != null;
    }

    public static boolean hasValidEnchantingFuel(ItemStack fuelStack, int level, CostRegistry costRegistry) {
        return findValidEnchantingFuel(fuelStack, level, costRegistry) != null;
    }

    public static boolean hasValidCostAndFuel(ItemStack costStack, ItemStack fuelStack,
                                              Identifier enchantmentId, int level,
                                              Player player, CostRegistry costRegistry) {
        return hasValidCost(costStack, enchantmentId, level, player, costRegistry)
                && hasValidEnchantingFuel(fuelStack, level, costRegistry);
    }

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

    private static Cost findValidEnchantingFuel(ItemStack fuelStack, int level, CostRegistry costRegistry) {
        List<Cost> validFuels = costRegistry.get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(level).costs();

        for(Cost validFuel : validFuels) {
            for(ItemStack validFuelStack : validFuel.getItemStacks()) {
                if(isCostStackValid(fuelStack, validFuelStack)) return validFuel;
            }
        }

        return null;
    }

    private static boolean isCostStackValid(ItemStack costStack, ItemStack validCostStack) {
        return costStack.getItem().equals(validCostStack.getItem()) && costStack.count() >= validCostStack.count();
    }

    private static boolean playerHasEnoughLevels(Player player, Cost validCost) {
        return player.experienceLevel >= validCost.xpLevels();
    }

    public static void deductValidCost(EnchantingTableMenu menu,
                                       Identifier enchantmentId, int level,
                                       Player player, CostRegistry costRegistry) {
        if(player.hasInfiniteMaterials()) return;

        ItemStack costStack = menu.getCostSlot().getItem();
        ItemStack fuelStack = menu.getFuelSlot().getItem();
        if(!hasValidCostAndFuel(costStack, fuelStack, enchantmentId, level, player, costRegistry)) throw new IllegalStateException("Cannot deduct cost as there is no valid cost!");

        Cost validCost = findValidCost(costStack, enchantmentId, level, player, costRegistry);
        Cost validFuel = findValidEnchantingFuel(fuelStack, level, costRegistry);

        menu.getCostSlot().getItem().shrink(validCost.amount());
        menu.getFuelSlot().getItem().shrink(validFuel.amount());

        player.giveExperienceLevels(-validCost.xpLevels());
    }

    public static List<Holder.Reference<Enchantment>> getAllRegisteredEnchantments(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Registries.ENCHANTMENT).listElements().toList();
    }
}
