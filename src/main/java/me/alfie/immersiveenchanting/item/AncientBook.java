package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedNBT;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;


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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        ResourceKey<Enchantment> enchantmentResourceKey = getStoredEnchantment(stack);
        if (enchantmentResourceKey != null) {
            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            Holder<Enchantment> enchantmentHolder = EnchantmentUtil
                    .getEnchantmentHolder(registryAccess, enchantmentResourceKey)
                    .orElse(null);
            if (enchantmentHolder == null) return;
            Enchantment enchantment = enchantmentHolder.value();

            MutableComponent loreText = Component.translatable("lore.immersiveenchanting.ancient_book").withStyle(ChatFormatting.GOLD);
            String enchantName = enchantment.getDescriptionId();
            Component fullTooltip = loreText.append(" ").append(Component.translatable(enchantName));
            tooltipComponents.add(fullTooltip);

            //Add replicated tooltip
            if(ReplicatedNBT.isReplicated(stack)) {
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
        ListTag listtag = new ListTag();
        ResourceLocation resourcelocation = enchantmentHolder.unwrapKey().get().location();

        listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, 1));

        stack.getOrCreateTag().put("StoredEnchantments", listtag);
    }

    /**
     * Get the enchantment in an ancient book and automatically migrate old NBT.
     * @param bookStack
     * @return
     */
    public static ResourceKey<Enchantment> getStoredEnchantment(ItemStack bookStack) {
        ListTag listTag = getEnchantments(bookStack);
        if (listTag.isEmpty()) return null;

        CompoundTag tag = (CompoundTag) listTag.get(0);
        ResourceLocation enchantmentRL = ResourceLocation.tryParse(tag.getString("id"));
        if(enchantmentRL == null) return null;

        return ResourceKey.create(Registries.ENCHANTMENT, enchantmentRL);
    }
}

