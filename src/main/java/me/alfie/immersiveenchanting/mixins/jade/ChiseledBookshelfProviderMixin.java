package me.alfie.immersiveenchanting.mixins.jade;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.addon.vanilla.ChiseledBookshelfProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.function.Consumer;

@Mixin(ChiseledBookshelfProvider.class)
public abstract class ChiseledBookshelfProviderMixin {

    @WrapWithCondition(
            method = "appendTooltip(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/BlockAccessor;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments;addToTooltip" +
                            "(Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;" +
                            "Lnet/minecraft/world/item/TooltipFlag;)V"
            )
    )
    private boolean immersiveenchanting$skipVanillaEnchantments(
            ItemEnchantments instance, Item.TooltipContext holder, Consumer<Component> holder1, TooltipFlag entry, @Local(name = "item") ItemStack stack
    ) {
        // Only run vanilla addToTooltip if stack is not an Ancient Book
        return stack.isEmpty() || !stack.is(ModItems.ANCIENT_BOOK.get());
    }

    @Inject(
            method = "appendTooltip(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/BlockAccessor;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("TAIL")
    )
    private void addCustomAncientBookTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config, CallbackInfo ci, @Local(name = "item") ItemStack stack) {
        if (stack.is(ModItems.ANCIENT_BOOK.get()) && stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            ResourceKey<Enchantment> key = AncientBook.getStoredEnchantment(stack, accessor.getLevel());
            if (key != null) {
                accessor.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolder(key.location())
                        .ifPresent(enchantment ->
                                tooltip.add(Component.translatable(AncientBook.TRANSLATION_KEY)
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(" ")
                                        .append(enchantment.value().description())
                                )
                        );
            }
        }
    }
}
