package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AncientBook extends EnchantedBookItem {

    public AncientBook(Properties properties) {
        super(properties
                .stacksTo(16)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return true;
    }

    /**Port info: 1.20.1 handles tooltips differently. Can hide tooltip parts.*/
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        //Mask enchantments
        ItemStack copy = stack.copy();
        if(copy.getOrCreateTag().contains(EnchantedBookItem.TAG_STORED_ENCHANTMENTS)) {
            copy.getTag().remove(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
        }

        super.appendHoverText(copy, level, tooltipComponents, tooltipFlag);

        if(level == null) return;
        Holder<Enchantment> enchantment = EnchantmentUtil.getStoredEnchantment(stack, level.registryAccess());

        MutableComponent component = Component.empty();
        if (enchantment != null) {
            List<Holder<Enchantment>> enchantments = List.of(enchantment);
            for(Holder<Enchantment> enchantmentHolder : enchantments) {
                component
                        .append(Component.translatable("item.immersiveenchanting.ancient_book.desc.enchantment"))
                        .append(" ")
                        .append(Component.translatable(enchantment.get().getDescriptionId()));
                tooltipComponents.add(component.withStyle(ChatFormatting.GOLD));

                if(ClientConfig.isShowAddedByTooltipEnabled()) {
                    String modNamespace = enchantmentHolder.unwrapKey().get().location().getNamespace();

                    ModInfo modInfo = (ModInfo) ModList.get().getModContainerById(modNamespace)
                            .map(ModContainer::getModInfo)
                            .orElse(null);

                    String modName = modInfo != null ? modInfo.getDisplayName() : modNamespace;

                    tooltipComponents.add(
                            Component.translatable("item.immersiveenchanting.ancient_book.desc.enchantment_added_by", modName)
                                    .withStyle(ChatFormatting.BLUE)
                    );
                }
            }
        }

        if(EnchantmentUtil.isReplicated(stack)) tooltipComponents.add(
                Component.translatable("item.immersiveenchanting.ancient_book.desc.replicated")
                        .withStyle(ChatFormatting.GRAY)
        );
    }


}
