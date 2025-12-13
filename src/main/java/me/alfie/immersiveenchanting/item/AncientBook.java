package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.datacomponents.EnchantmentDataComponent;
import me.alfie.immersiveenchanting.datacomponents.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.List;

public class AncientBook extends Item {


    public AncientBook(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        //Get data component for this item stack.
        EnchantmentDataComponent enchantmentDataComponent = stack.get(ModDataComponents.ENCHANTMENT.get());
        if (enchantmentDataComponent != null) {
            //Get the enchantment from the registry.

            //RegistryAccess access = context.level().registryAccess();
            //context.registries().lookupOrThrow(Registries.ENCHANTMENT);
            //List<Holder.Reference<Enchantment>> allEnchantments = lookup.listElements().toList();
            //Registry<Enchantment> enchantmentRegistry = access.registryOrThrow(Registries.ENCHANTMENT);

            RegistryAccess registryAccess;
            registryAccess = Minecraft.getInstance().level.registryAccess();
            Registry<Enchantment> enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

            Enchantment enchantment = enchantmentRegistry.get(ResourceLocation.parse(enchantmentDataComponent.enchantmentResourceLocation()));

            //Translation key for lore text.
            MutableComponent loreText = Component.translatable("lore.immersiveenchanting.ancient_book");

            //Enchantment name (styled)
            MutableComponent enchantName = (MutableComponent) enchantment.description();
            loreText.withStyle(ChatFormatting.GOLD);

            //Combine lore text + enchantment name
            Component fullTooltip = loreText.append(" ").append(enchantName);

            //Add to tooltip list
            tooltipComponents.add(fullTooltip);

            //Added by mod tooltip
            ResourceLocation enchantmentResourceLocation = ResourceLocation.parse(enchantmentDataComponent.enchantmentResourceLocation());
            String modNamespace = enchantmentResourceLocation.getNamespace();

            ModInfo modInfo = (ModInfo) ModList.get().getModContainerById(modNamespace)
                    .map(ModContainer::getModInfo)
                    .orElse(null);

            if (modInfo != null) {
                String modName = modInfo.getDisplayName();
                Component addedBy = Component.literal("Added by " + modName)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                tooltipComponents.add(addedBy);
            } else {
                Component addedBy = Component.literal("Added by " + modNamespace)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                tooltipComponents.add(addedBy);
            }
        }

    }
}

