package me.alfie.immersiveenchanting.item;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        //Get data component for this item stack.
        ResourceLocation enchantmentRL = AncientBookNBT.getEnchantment(stack).location();
        if (enchantmentRL != null) {
            //Get the enchantment from the registry.
            RegistryAccess registryAccess;
            registryAccess = Minecraft.getInstance().level.registryAccess();
            Registry<Enchantment> enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
            Enchantment enchantment = enchantmentRegistry.get(enchantmentRL);

            //Translation key for lore text.
            MutableComponent loreText = Component.translatable("lore.immersiveenchanting.ancient_book");

            //Enchantment name (styled)
            String enchantName = enchantment.getDescriptionId();
            loreText.withStyle(ChatFormatting.GOLD);

            //Combine lore text + enchantment name
            Component fullTooltip = loreText.append(" ").append(Component.translatable(enchantName));

            //Add to tooltip list
            tooltipComponents.add(fullTooltip);

            //Added by mod tooltip
            String modNamespace = enchantmentRL.getNamespace();

            IModInfo modInfo = ModList.get().getModContainerById(modNamespace)
                    .map(ModContainer::getModInfo)
                    .orElse(null);

            if (modInfo != null) {
                String modName = modInfo.getDisplayName();
                Component addedBy = Component.translatable("lore.immersiveenchanting.added_by").append(" " + modName).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                tooltipComponents.add(addedBy);
            } else {
                Component addedBy = Component.translatable("lore.immersiveenchanting.added_by").append(" " + modNamespace).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                tooltipComponents.add(addedBy);
            }
        }

    }
}

