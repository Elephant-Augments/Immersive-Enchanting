package me.alfie.immersiveenchanting.mixin;

import com.google.common.collect.Lists;
import me.alfie.immersiveenchanting.config.ServerConfig;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(AbstractVillager.class)
public class VillagerTradeMixin {

    @Redirect(
            method = "addOffersFromItemListings",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean immersiveenchanting$removeEnchantedBookTrades(MerchantOffers offers, Object offerObj) {
        MerchantOffer offer = (MerchantOffer) offerObj;

        if (ServerConfig.areEnchantedBookTradesDisabled() && offer.getResult().is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        return offers.add(offer);
    }
}

