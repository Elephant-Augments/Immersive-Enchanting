package me.alfie.immersiveenchanting.networking.packet.enchantitem;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.compat.ModCompat;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostDatapack;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

        AbstractContainerMenu enchantingTableMenu = player.containerMenu;
        ItemStack itemToEnchant = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal()).getItem();

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
            level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1, 1);
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

            if (packet.enchantmentLevel() == EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment()).getHighestLevel()) {
                //Sound FX for highest tier.
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                //Sound FX for normal tier.
                level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            //If unable to enchant
            level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
