package me.alfie.immersiveenchanting.mixin;

import me.alfie.immersiveenchanting.config.ServerConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrade.class)
public class VillagerTradeMixin {

    @ModifyVariable(
            method = "getOffer",
            at = @At("STORE"),
            name = "result")
    private ItemStack immersiveenchanting$removeEnchantedBookTrades(ItemStack result) {

        if (result.is(Items.ENCHANTED_BOOK) && ServerConfig.areEnchantedBookTradesDisabled()) {
            return ItemStack.EMPTY;
        }

        return result;
    }
}

