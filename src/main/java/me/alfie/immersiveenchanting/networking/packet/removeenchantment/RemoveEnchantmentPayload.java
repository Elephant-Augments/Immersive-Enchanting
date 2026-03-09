package me.alfie.immersiveenchanting.networking.packet.removeenchantment;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public class RemoveEnchantmentPayload implements PayloadHandler<RemoveEnchantmentPacket> {
    @Override //Empty
    public void execOnClient(RemoveEnchantmentPacket packet, IPayloadContext context) {}

    @Override
    public void execOnServer(RemoveEnchantmentPacket packet, IPayloadContext context) {
        Player player = context.player();
        if (player == null) return;
        Level level = player.level();

        AbstractContainerMenu enchantingTableMenu = player.containerMenu;
        ItemStack itemToEnchant = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal()).getItem();

        Optional<Holder.Reference<Enchantment>> enchantmentHolder = EnchantmentUtil.getEnchantmentHolder(
                level.registryAccess(),
                packet.enchantment());

        Holder<Enchantment> enchantment = enchantmentHolder.orElseThrow(() ->
                new IllegalStateException("Enchantment not found: " + packet.enchantment())
        );


        // Get mutable enchantments from the item
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemToEnchant.getTagEnchantments());
        int levelToRemove = packet.enchantmentLevel();

        int currentLevel = mutable.getLevel(enchantment);

        if (currentLevel > 0) {
            // Compute next level down
            int newLevel = currentLevel;

            if (currentLevel >= levelToRemove) {
                // Remove the specified level
                newLevel = levelToRemove - 1; // next level down
            }

            // Update the mutable map
            if (newLevel > 0) {
                mutable.set(enchantment, newLevel); // set new level
            } else {
                // Remove entirely if we drop below 1
                mutable.removeIf(e -> e.equals(enchantment));
            }
        }

        ItemEnchantments newEnchantments = mutable.toImmutable();
        EnchantmentHelper.setEnchantments(itemToEnchant, newEnchantments);
        FxHelper.playEnchantmentRemoveSound(level, player);
    }
}
