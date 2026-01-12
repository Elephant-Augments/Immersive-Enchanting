package me.alfie.immersiveenchanting.mixins.jade;

import com.llamalad7.mixinextras.sugar.Local;
import me.alfie.immersiveenchanting.datacomponents.EnchantmentDataComponent;
import me.alfie.immersiveenchanting.datacomponents.ModDataComponents;
import me.alfie.immersiveenchanting.item.AncientBook;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.addon.vanilla.ChiseledBookshelfProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(ChiseledBookshelfProvider.class)
public abstract class ChiseledBookshelfProviderMixin {
    @Inject(method = "appendTooltip(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/BlockAccessor;Lsnownee/jade/api/config/IPluginConfig;)V", at = @At("TAIL"))
    private void immersiveenchanting$addTooltip(final ITooltip tooltip, final BlockAccessor accessor, final IPluginConfig config, final CallbackInfo callback, @Local(name = "item") final ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        EnchantmentDataComponent enchantmentData = stack.get(ModDataComponents.ENCHANTMENT);

        if (enchantmentData == null) {
            return;
        }

        String resource = enchantmentData.enchantmentResourceLocation();

        if (resource == null) {
            return;
        }

        accessor.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ResourceLocation.parse(resource))
                .ifPresent(enchantment -> tooltip.add(
                        Component.translatable(AncientBook.TRANSLATION_KEY)
                                .withStyle(ChatFormatting.GOLD)
                                .append(" ")
                                .append(enchantment.value().description())
                ));
    }
}
