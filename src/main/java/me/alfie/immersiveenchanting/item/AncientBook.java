package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.compat.ModCheck;
import me.alfie.immersiveenchanting.compat.ModCompat;
import me.alfie.immersiveenchanting.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AncientBook extends EnchantedBookItem {


    public AncientBook(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        //Get data component for this item stack.
        ResourceKey<Enchantment> enchantmentResourceKey = AncientBook.getEnchantment(stack, level);
        if (enchantmentResourceKey != null) {
            //Get the enchantment from the registry.
            Registry<Enchantment> enchantmentRegistry = ImmersiveEnchanting.getEnchantmentRegistry(
                    Minecraft.getInstance().level.registryAccess()
            );
            Enchantment enchantment = enchantmentRegistry.get(enchantmentResourceKey);
            if(enchantment == null) return;

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
            if(ClientConfig.isShowAddedByTooltipEnabled()) {
                ResourceLocation enchantmentRL = enchantmentResourceKey.location();
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

    public static void setStoredEnchantment(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        ListTag listtag = new ListTag();
        ResourceLocation resourcelocation = enchantmentHolder.unwrapKey().get().location();

        listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, 1));

        stack.getOrCreateTag().put("StoredEnchantments", listtag);
    }

    /**
     * Get the enchantment in an ancient book and automatically migrate old NBT.
     * @param bookStack
     * @param level Used to migrate NBT.
     * @return
     */
    public static ResourceKey<Enchantment> getEnchantment(ItemStack bookStack, Level level) {
        migrateNBT(bookStack, level);

        ListTag listTag = getEnchantments(bookStack);
        CompoundTag tag = (CompoundTag) listTag.get(0);
        ResourceLocation enchantmentRL = ResourceLocation.parse(tag.getString("id"));
        return ResourceKey.create(Registries.ENCHANTMENT, enchantmentRL);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /**
     * Migrate old NBT from ImmersiveEnchanting 2.x.x to StoredEnchantments <br>
     * Server-side only.
     * @param stack
     */
    public static void migrateNBT(ItemStack stack, Level level) {
        if(level == null || level.isClientSide()) return;
        if(!stack.hasTag()) return;

        CompoundTag tag = stack.getTag();
        //Already migrated
        if (tag.contains("StoredEnchantments")) return;

        String oldNBT = ImmersiveEnchanting.MODID+":ancient_book_enchantment_type";
        if (tag.contains(oldNBT)) {

            ResourceKey<Enchantment> enchantmentResourceKey = ResourceKey.create(
                    Registries.ENCHANTMENT, ResourceLocation.tryParse(tag.getString(oldNBT))
            );

            Optional<Holder.Reference<Enchantment>> enchantmentHolder = ImmersiveEnchanting.getEnchantmentHolder(Minecraft.getInstance().level.registryAccess(), enchantmentResourceKey);
            enchantmentHolder.ifPresent(enchantmentReference -> setStoredEnchantment(stack, enchantmentReference));

            tag.remove(oldNBT);
        }
    }

    /**
     * Migrate NBT in player inventory.
     * @param stack
     * @param level
     * @param entity
     * @param slot
     * @param selected
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        migrateNBT(stack, level);
    }
}

