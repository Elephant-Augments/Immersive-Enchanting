package me.alfie.immersiveenchanting.networking.packet.enchantitem;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.compat.ModCompat;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostDatapack;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnchantItemPayload implements PayloadHandler<EnchantItemPacket> {
    @Override //Empty
    public void execOnClient(EnchantItemPacket packet, IPayloadContext context) {}

    @Override
    public void execOnServer(EnchantItemPacket packet, IPayloadContext context) {
        Player player = context.player();
        if (player == null) return;
        Level level = player.level();

        EnchantingTableMenu enchantingTableMenu = (EnchantingTableMenu) player.containerMenu;
        ItemStack itemToEnchant = enchantingTableMenu.getToolSlotItem();

        BlockPos tablePos = enchantingTableMenu.getBlockPos();

        RegistryAccess registryAccess = player.registryAccess();
        Optional<Holder.Reference<Enchantment>> enchantmentHolder = ImmersiveEnchanting.getEnchantmentHolder(
                registryAccess,
                packet.enchantment()
        );

        Holder<Enchantment> enchantment = enchantmentHolder.orElseThrow(() ->
                new IllegalStateException("Enchantment not found: " + packet.enchantment())
        );

        // Currently only for Enchant Limiter.
        // Currently, this check only exists on NeoForge 1.21.1 as Enchant Limiter is not available on Forge 1.20.1.
        if (!ModCompat.canEnchant(itemToEnchant, enchantment)) {
            FxHelper.playEnchantFailFx(level, tablePos);
            return;
        }

        //Check enchantment cost
        ItemStack costSlotItemStack = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.COST.ordinal()).getItem();
        List<ItemStack> insertedItems = new ArrayList<>();
        insertedItems.add(costSlotItemStack);

        int playerXp = player.experienceLevel;

        //TODO Use .isCostValid?
        CostDefinition costNode = EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment()).getCostForLevel(packet.enchantmentLevel());
        boolean costSlotIsValid = CostHelper.isCostValid(costNode, insertedItems, playerXp);

        List<Item> validEnchantingFuels = EnchantmentCostDatapack.getValidEnchantingFuels();
        ItemStack enchantingFuel = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.ENCHANTING_FUEL.ordinal()).getItem();
        boolean enchantingFuelIsValid = validEnchantingFuels.contains(enchantingFuel.getItem());

        boolean hasEnoughCost = player.hasInfiniteMaterials() ||
                ( (validEnchantingFuels.isEmpty() || enchantingFuelIsValid)
                        && costSlotIsValid);

        if (hasEnoughCost) {
            if (!player.hasInfiniteMaterials()) {
                enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.ENCHANTING_FUEL.ordinal()).getItem()
                        .shrink(1); //Todo use config

                //TODO shrink by correct amount from cost
                //costSlotItemStack.shrink(requiredItemCostStack.getCount()); //Use enchantment cost if not air
            }

            //Enchant item server side
            itemToEnchant.enchant(
                    enchantment,
                    packet.enchantmentLevel()
            );

            player.awardStat(Stats.ENCHANT_ITEM);

            if (player instanceof ServerPlayer serverPlayer) {
                // Number is levels spent - using 1 to as a compatible default value
                // (Adjust if optional enchantment cost extensions in the future may include xp cost)
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, itemToEnchant, 1);
            }

            boolean isHighestTier = packet.enchantmentLevel() == EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment()).getHighestLevel();
            FxHelper.playEnchantSuccessFx(level, tablePos, isHighestTier);
        } else {
            FxHelper.playEnchantFailFx(level, tablePos);
        }
    }
}
