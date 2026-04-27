package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.item.AncientBook;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class providing helper methods for working with enchantments.
 *
 * <p>This class centralises common logic related to enchantment handling,
 * including:</p>
 * <ul>
 *     <li>Converting between {@link ResourceLocation} and {@link Holder} representations</li>
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

    public static final String REPLICATED_NBT_TAG = "replicated";

    /**
     * Resolves an enchantment identifier into a {@link Holder}.
     *
     * @param id     the enchantment identifier
     * @param access the registry access used to look up the enchantment
     * @return the corresponding {@link Holder<Enchantment>}
     * @throws java.util.NoSuchElementException if the enchantment is not present
     */
    public static Holder<Enchantment> toHolder(ResourceLocation id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT)
                .get(ResourceKey.create(Registries.ENCHANTMENT, id))
                .orElseThrow();
    }

    public static List<Holder<Enchantment>> toHolders(List<ResourceKey<Enchantment>> resourceKeys, RegistryAccess registryAccess) {
        List<Holder<Enchantment>> result = new ArrayList<>();
        for (ResourceKey<Enchantment> resourceKey : resourceKeys) {
            result.add(toHolder(resourceKey.location(), registryAccess));
        }
        return result;
    }

    public static ResourceKey<Enchantment> toResourceKey(ResourceLocation id) {
        return ResourceKey.create(Registries.ENCHANTMENT, id);
    }

    public static List<ResourceKey<Enchantment>> toResourceKeys(List<Holder<Enchantment>> enchantmentHolders) {
        List<ResourceKey<Enchantment>> result = new ArrayList<>();
        for(Holder<Enchantment> enchantmentHolder : enchantmentHolders) {
            result.add(enchantmentHolder.unwrapKey().get());
        }
        return result;
    }

    /**
     * Converts an enchantment holder into its identifier.
     *
     * @param enchantmentHolder the enchantment holder
     * @return the identifier of the enchantment
     * @throws java.util.NoSuchElementException if the holder has no registry key
     */
    public static ResourceLocation toId(Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.unwrapKey().orElseThrow().location();
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
        ListTag listtag = new ListTag();
        ResourceLocation resourcelocation = enchantmentHolder.unwrapKey().get().location();

        listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, 1));

        ancientBook.getOrCreateTag().put(EnchantedBookItem.TAG_STORED_ENCHANTMENTS, listtag);
    }

    /**
     * Retrieves the stored enchantment identifier from an "ancient book".
     *
     * <p>If multiple enchantments are present, only the first is returned.</p>
     *
     * @param ancientBook the item stack to read from
     * @return the stored enchantment holder, or {@code null} if none is present
     */
    public static Holder<Enchantment> getStoredEnchantment(ItemStack ancientBook, RegistryAccess registryAccess) {
        if(!(ancientBook.getItem() instanceof AncientBook)) return null;
        ListTag listTag = EnchantedBookItem.getEnchantments(ancientBook);
        if (listTag.isEmpty()) return null;

        CompoundTag tag = (CompoundTag) listTag.get(0);
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        if(id == null) return null;

        return toHolder(id, registryAccess);
    }

    public static void setReplicatedNbtTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(REPLICATED_NBT_TAG, true);
    }

    public static boolean isReplicated(ItemStack stack) {
        if(stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if(tag.contains(REPLICATED_NBT_TAG)) {
                return tag.getBoolean(REPLICATED_NBT_TAG);
            }
        }
        return false;
    }

    public static List<Holder<Enchantment>> sortByName(List<Holder<Enchantment>> enchantmentHolders) {
        enchantmentHolders.sort(Comparator.comparing(enchantmentHolder -> enchantmentHolder.value().getDescriptionId()));
         return enchantmentHolders;
    }

    public static boolean canEnchant(EnchantingTableMenu menu,
                                     Holder<Enchantment> enchantmentHolder, int level,
                                     NetworkEvent.Context context) {
        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        if(!isNextLevel(stackToEnchant, enchantmentHolder, level)) return false;
        if(!enchantmentHolder.get().canEnchant(menu.getToolSlot().getItem())) return false;
        if(!isEnchantmentAvailableInBookshelves(enchantmentHolder, menu, context)) return false;

        if(context.getSender().isCreative()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), toId(enchantmentHolder), level, context.getSender(), CostRegistry.server())) return false;

        return true;
    }

    public static boolean canTransmute(EnchantingTableMenu menu, Holder<Enchantment> newEnchantment, NetworkEvent.Context context) {
        if(!isEnchantmentAvailableInBookshelves(newEnchantment, menu, context)) return false;
        if(EnchantmentUtil.isReplicated(menu.getToolSlot().getItem())) return false;

        if(context.getSender().isCreative()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), CostRegistry.TRANSMUTE, 1, context.getSender(), CostRegistry.server())) return false;

        return true;
    }

    public static boolean canReplicate(EnchantingTableMenu menu, NetworkEvent.Context context) {
        if(context.getSender().isCreative()) return true;
        if(!hasValidCostAndFuel(menu.getCostSlot().getItem(), menu.getFuelSlot().getItem(), CostRegistry.REPLICATE, 1, context.getSender(), CostRegistry.server())) return false;

        return true;
    }

    private static boolean isEnchantmentAvailableInBookshelves(Holder<Enchantment> enchantmentHolder, EnchantingTableMenu menu, NetworkEvent.Context context) {
        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), context.getSender().level());
        return availableEnchantments.contains(enchantmentHolder);
    }

    private static boolean isNextLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder, int level) {
        int equippedLevel = stack.getEnchantmentLevel(enchantmentHolder.get());
        return level == equippedLevel + 1;
    }

    public static boolean hasValidCost(ItemStack costStack,
                                       ResourceLocation enchantmentId, int level,
                                       Player player, CostRegistry costRegistry) {
        return findValidCost(costStack, enchantmentId, level, player, costRegistry) != null;
    }

    public static boolean hasValidEnchantingFuel(ItemStack fuelStack, int level, CostRegistry costRegistry) {
        return findValidEnchantingFuel(fuelStack, level, costRegistry) != null;
    }

    public static boolean hasValidCostAndFuel(ItemStack costStack, ItemStack fuelStack,
                                              ResourceLocation enchantmentId, int level,
                                              Player player, CostRegistry costRegistry) {
        return hasValidCost(costStack, enchantmentId, level, player, costRegistry)
                && hasValidEnchantingFuel(fuelStack, level, costRegistry);
    }

    private static Cost findValidCost(ItemStack costStack, ResourceLocation enchantmentId, int level,
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
        return costStack.getItem().equals(validCostStack.getItem()) && costStack.getCount() >= validCostStack.getCount();
    }

    private static boolean playerHasEnoughLevels(Player player, Cost validCost) {
        return player.experienceLevel >= validCost.xpLevels();
    }

    public static void deductValidCost(EnchantingTableMenu menu,
                                       ResourceLocation enchantmentId, int level,
                                       Player player, CostRegistry costRegistry) {
        if(player.isCreative()) return;

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
