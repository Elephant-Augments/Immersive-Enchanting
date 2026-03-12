package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.List;
import java.util.Optional;


//Now handled as an EnchantedBookItem .enchant() uses the STORED_ENCHANTMENTS data component.
public class AncientBook extends EnchantedBookItem {

    public static final String TRANSLATION_KEY = "lore.immersiveenchanting.ancient_book";

    public AncientBook(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { 
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ResourceKey<Enchantment> enchantmentResourceKey = getStoredEnchantment(stack, context.level());
        if (enchantmentResourceKey != null) {
            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            Holder<Enchantment> enchantmentHolder = EnchantmentUtil
                    .getEnchantmentHolder(registryAccess, enchantmentResourceKey)
                    .orElse(null);
            if (enchantmentHolder == null) return;

            Enchantment enchantment = enchantmentHolder.value();

            MutableComponent loreText = Component.translatable(TRANSLATION_KEY).withStyle(ChatFormatting.GOLD);
            MutableComponent enchantName = (MutableComponent) enchantment.description();

            Component fullTooltip = loreText.append(" ").append(enchantName);
            tooltipComponents.add(fullTooltip);

            //Add replicated tooltip
            if(ReplicatedDataComponent.isReplicated(stack)) {
                Component replicatedHint = Component.translatable("lore.immersiveenchanting.replicated")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                tooltipComponents.add(replicatedHint);
            }

            //Added by mod tooltip
            if(ClientConfig.isShowAddedByTooltipEnabled()) {
                ResourceLocation enchantmentId = enchantmentResourceKey.location();
                String modNamespace = enchantmentId.getNamespace();
                ModInfo modInfo = (ModInfo) ModList.get().getModContainerById(modNamespace)
                        .map(ModContainer::getModInfo)
                        .orElse(null);

                String modName = modInfo != null ? modInfo.getDisplayName() : modNamespace;

                Component addedBy = Component.translatable("lore.immersiveenchanting.added_by")
                        .append(" " + modName)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

                tooltipComponents.add(addedBy);
            }
        }

    }

    public static void setStoredEnchantment(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        ItemEnchantments.Mutable itemenchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        itemenchantments.set(enchantmentHolder, 1);
        stack.set(DataComponents.STORED_ENCHANTMENTS, itemenchantments.toImmutable());
    }

    /**
     * Get the stored enchantment of a book and automatically migrate old data.
     * @param bookStack
     * @param level Required to migrate data component (server-side)
     * @return
     */
    public static ResourceKey<Enchantment> getStoredEnchantment(ItemStack bookStack, Level level) {
        ItemEnchantments itemEnchantments = bookStack.get(DataComponents.STORED_ENCHANTMENTS);
        if(itemEnchantments == null) {
            return null;
        }

        List<Holder<Enchantment>> enchantments = itemEnchantments.keySet().stream().toList();
        if (enchantments.isEmpty()) return null;

        Holder<Enchantment> enchantmentHolder = enchantments.getFirst();
        return enchantmentHolder.getKey();
    }
}

