package me.alfie.immersiveenchanting.util;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostHolder;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class CostHelper {
    public static boolean canEnchant(EnchantingTableMenu menu,
                                     Holder<Enchantment> enchantmentHolder, int level,
                                     Player player) {
        if(!CostRegistry.server().isRegistered(enchantmentHolder) ||
            !CostRegistry.server().isRegistered(CostRegistry.ENCHANTING_FUELS)) return false;
        if(!CostRegistry.server().get(enchantmentHolder).enabled()) return false;
        if(!isEnchantmentAvailableInBookshelves(enchantmentHolder, menu, player)) return false;

        ItemStack stackToEnchant = menu.getToolSlot().getItem();

        if(!stackToEnchant.is(Items.BOOK)) {
            if(!isEnchantmentNextLevel(stackToEnchant, enchantmentHolder, level)) return false;
            if(!stackToEnchant.supportsEnchantment(enchantmentHolder)) return false;
        }

        if(isTooExpensive(stackToEnchant, EnchantmentUtil.toId(enchantmentHolder), level, player)) {
            return false;
        }

        return tryConsumeValidCostAndFuel(
                EnchantmentUtil.toId(enchantmentHolder), level,
                menu, player);
    }

    public static boolean canTransmute(EnchantingTableMenu menu, Holder<Enchantment> oldEnchantment, Player player) {
        if(!CostRegistry.server().isRegistered(CostRegistry.TRANSMUTE)) return false;
        if(!CostRegistry.server().get(CostRegistry.TRANSMUTE).enabled()) return false;
        if(!isEnchantmentAvailableInBookshelves(oldEnchantment, menu, player)) return false;
        if(EnchantmentUtil.isReplicated(menu.getToolSlot().getItem())) return false;

        return tryConsumeValidCostAndFuel(
                CostRegistry.TRANSMUTE, 1,
                menu, player);
    }

    public static boolean canReplicate(EnchantingTableMenu menu, Player player) {
        if(!CostRegistry.server().isRegistered(CostRegistry.REPLICATE)) return false;
        if(!CostRegistry.server().get(CostRegistry.REPLICATE).enabled()) return false;

        return tryConsumeValidCostAndFuel(
                CostRegistry.REPLICATE, 1,
                menu, player);
    }

    /**
     * Blocks enchantment applications when the projected sum of datapack {@code xp_levels} 
     * on the item would exceed {@link ServerConfig#getMaxItemEnchantmentXpCost()}.
     */
    public static boolean isTooExpensive(ItemStack stack, ResourceId costId, int level, Player player) {
        if(player.hasInfiniteMaterials()) return false;
        return projectedEnchantmentXpTotal(stack, costId, level, CostRegistry.server())
                > ServerConfig.getMaxItemEnchantmentXpCost();
    }

    /**
     * Client-side mirror of {@link #isTooExpensive} using the client cost registry.
     */
    public static boolean isTooExpensiveClient(ItemStack stack, ResourceId costId, int level, Player player) {
        if(player == null || player.hasInfiniteMaterials()) return false;
        return projectedEnchantmentXpTotal(stack, costId, level, CostRegistry.client())
                > ServerConfig.getMaxItemEnchantmentXpCost();
    }

    /**
     * Sum of datapack XP costs for enchantments that would be on the stack after applying
     * the incoming enchantment.
     */
    public static int projectedEnchantmentXpTotal(
            ItemStack stack, ResourceId incomingId, int incomingLevel, CostRegistry registry
    ) {
        int total = 0;
        for(Holder<Enchantment> holder : EnchantmentUtil.getEnchantments(stack).keySet()) {
            ResourceId id = EnchantmentUtil.toId(holder);
            if(id.equals(incomingId)) continue;
            total += xpCostFor(id, EnchantmentUtil.getEnchantmentLevel(stack, holder), registry);
        }
        total += xpCostFor(incomingId, incomingLevel, registry);
        return total;
    }

    /**
     * Calculates total cost of an enchantment based on datapack {@code xp_levels} entries, 
     * or 0 if unset. Uses the first defined cost entry for that level.
     */
    public static int xpCostFor(ResourceId costId, int level, CostRegistry registry) {
        if(!registry.isRegistered(costId)) return 0;
        CostHolder costHolder = registry.get(costId).levelCosts().getLevel(level);
        if(costHolder.costs().isEmpty()) return 0;
        return costHolder.costs().get(0).xpLevels();
    }

    private static boolean isEnchantmentAvailableInBookshelves(Holder<Enchantment> enchantmentHolder, EnchantingTableMenu menu, Player player) {
        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), player.level());
        return availableEnchantments.contains(enchantmentHolder);
    }

    private static boolean isEnchantmentNextLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder, int level) {
        int equippedLevel = EnchantmentUtil.getEnchantmentLevel(stack, enchantmentHolder);
        return level == equippedLevel + 1;
    }

    private static boolean tryConsumeValidCostAndFuel(ResourceId costId,
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
        player.giveExperienceLevels(-validCost.xpLevels());
        return true;
    }
}
