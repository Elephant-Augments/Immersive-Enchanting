package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datacomponent.EnchantmentDataComponent;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
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
            Holder<Enchantment> enchantmentHolder = ImmersiveEnchanting
                    .getEnchantmentHolder(registryAccess, enchantmentResourceKey)
                    .orElse(null);
            if (enchantmentHolder == null) return;


            //Get the enchantment from the registry.
            ResourceLocation enchantmentResourceLocation = ResourceLocation.parse(enchantmentHolder.getRegisteredName());
            Registry<Enchantment> enchantmentRegistry = ImmersiveEnchanting.getEnchantmentRegistry(registryAccess);
            Enchantment enchantment = enchantmentRegistry.get(enchantmentResourceKey);
            if (enchantment == null) return;

            //Translation key for lore text.
            MutableComponent loreText = Component.translatable(TRANSLATION_KEY);


            //Enchantment name (styled)
            MutableComponent enchantName = (MutableComponent) enchantment.description();

            loreText.withStyle(ChatFormatting.GOLD);

            //Combine lore text + enchantment name
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
                String modNamespace = enchantmentResourceLocation.getNamespace();
                ModInfo modInfo = (ModInfo) ModList.get().getModContainerById(modNamespace)
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
        migrateDataComponent(bookStack, level);

        ItemEnchantments itemEnchantments = bookStack.get(DataComponents.STORED_ENCHANTMENTS);
        if(itemEnchantments == null) {
            return null;
        }

        List<Holder<Enchantment>> enchantments = itemEnchantments.keySet().stream().toList();
        if (enchantments.isEmpty()) return null;

        Holder<Enchantment> enchantmentHolder = enchantments.getFirst();
        return enchantmentHolder.getKey();
    }

    /**
     * Migrate old data component from ImmersiveEnchanting 2.x.x to StoredEnchantments <br>
     * Server-side only, will not do anything client-side.
     * @param stack
     */
    public static void migrateDataComponent(ItemStack stack, Level level) {
        if(level == null || level.isClientSide()) return;
        if(stack.has(DataComponents.STORED_ENCHANTMENTS)) return;

        if (stack.has(ModDataComponents.LEGACY_ENCHANTMENT.get())) {
            EnchantmentDataComponent enchantmentDataComponent = stack.get(ModDataComponents.LEGACY_ENCHANTMENT.get());
            ResourceLocation resourceLocation = ResourceLocation.tryParse(enchantmentDataComponent.enchantmentResourceLocation());
            ResourceKey<Enchantment> enchantmentResourceKey = ResourceKey.create(Registries.ENCHANTMENT, resourceLocation);

            Optional<Holder.Reference<Enchantment>> enchantmentHolder = ImmersiveEnchanting.getEnchantmentHolder(level.registryAccess(), enchantmentResourceKey);
            enchantmentHolder.ifPresent(enchantmentReference -> setStoredEnchantment(stack, enchantmentReference));

            stack.remove(ModDataComponents.LEGACY_ENCHANTMENT.get());
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        migrateDataComponent(stack, level);
    }
}

