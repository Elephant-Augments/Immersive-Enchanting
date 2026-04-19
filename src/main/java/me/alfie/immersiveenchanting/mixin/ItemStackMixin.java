package me.alfie.immersiveenchanting.mixin;

import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    /**
     * Injects into the ItemStack tooltip building pipeline to customize how stored enchantments
     * are displayed for specific modded items.
     *
     * This hook targets the STORED_ENCHANTMENTS data component during tooltip construction and
     * replaces the default enchantment formatting when the ItemStack matches the
     * ModItems.ANCIENT_BOOK item.
     *
     * Instead of the vanilla enchantment list, a single formatted component is produced that
     * combines a custom translation key with each enchantment's description, styled in gold.
     *
     * This injection is purely presentational and does not modify underlying item data or
     * gameplay behavior.
     *
     * @param type      The DataComponentType currently being processed for tooltip rendering
     * @param context   The tooltip context (client-side formatting information)
     * @param display   Controls which tooltip elements are allowed to render
     * @param consumer  Output sink for tooltip components
     * @param flag      Tooltip visibility flags (advanced/shift toggles)
     */
    @Inject(
            method = "addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private <T extends net.minecraft.world.item.component.TooltipProvider>
    void immersiveenchanting$addToTooltip(DataComponentType<T> type, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        if (type == DataComponents.STORED_ENCHANTMENTS && self.is(ModItems.ANCIENT_BOOK.get())) {
            ItemEnchantments enchantments = self.get(DataComponents.STORED_ENCHANTMENTS);

            MutableComponent component = Component.empty();
            if (enchantments != null) {
                for(Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
                    component
                            .append(Component.translatable("item.immersiveenchanting.ancient_book.desc.enchantment"))
                            .append(" ")
                            .append(enchantmentHolder.value().description());
                }
                consumer.accept(component.withStyle(ChatFormatting.GOLD));
            }

            if(EnchantmentUtil.isReplicated(self)) consumer.accept(
                    Component.translatable("item.immersiveenchanting.ancient_book.desc.replicated")
                            .withStyle(ChatFormatting.GRAY)
            );

            ci.cancel(); //Prevent DataComponents.STORED_ENCHANTMENTS being applied normally to ancient books.
        }
    }
}
