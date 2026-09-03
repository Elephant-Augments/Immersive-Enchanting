package me.alfie.immersiveenchanting.mixin;

import me.alfie.immersiveenchanting.compat.enchdesc.EnchantmentDescriptionsCompat;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.util.EnchantmentTooltipColors;
import me.alfie.immersiveenchanting.util.EnchantmentTooltipOrdering;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles enchantment ordering and title colors on item tooltips *only* when
 * enchantment colors are enabled.
 */
@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {

    @Inject(
            method = "addToTooltip",
            at = @At("HEAD"),
            cancellable = true
    )
    private void immersiveenchanting$emitColoredTooltipLines(
            Item.TooltipContext context,
            Consumer<Component> consumer,
            TooltipFlag flag,
            CallbackInfo ci
    ) {
        if(!ClientConfig.isColorEnchantmentTooltipsByFilterEnabled()) {
            return;
        }

        ItemEnchantments enchantments = (ItemEnchantments)(Object)this;
        if(enchantments.isEmpty()) {
            return;
        }

        List<Holder<Enchantment>> keys = EnchantmentTooltipOrdering.needsCustomOrder(enchantments)
                ? EnchantmentTooltipOrdering.sortedKeys(enchantments)
                : new ArrayList<>(enchantments.keySet());

        for(Holder<Enchantment> enchantment : keys) {
            int level = enchantments.getLevel(enchantment);
            Component name = Enchantment.getFullname(enchantment, level);
            consumer.accept(EnchantmentTooltipColors.styleEnchantmentName(enchantment, name));
            EnchantmentDescriptionsCompat.appendDescription(enchantment, consumer);
        }
        ci.cancel();
    }
}
