package me.alfie.immersiveenchanting.gui.core.booktab;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;
import java.awt.*;

public class EnchantmentBox {

    public final BookTab bookTab;
    private final Holder<Enchantment> enchantmentHolder;

    public EnchantmentBox(BookTab bookTab, @Nullable Holder<Enchantment> enchantmentHolder) {
        this.bookTab = bookTab;
        this.enchantmentHolder = enchantmentHolder;
    }

    public void render(GuiGraphics guiGraphics, int x, int y) {
        // Determine which sprite to use based on whether the enchantment is unlocked
        Sprite boxSprite = bookTab.screen.getMenu().isEnchantmentUnlocked(enchantmentHolder) ?
                Sprite.ENCHANTMENT_BOX_UNLOCKED : Sprite.ENCHANTMENT_BOX_LOCKED;

        // Render the sprite
        final int spriteWidth = 108;
        final int spriteHeight = 19;
        guiGraphics.blit(
                boxSprite.get(),
                x,
                y,
                0f, 0f, spriteWidth, spriteHeight,
                spriteWidth, spriteHeight
        );

        // Render the enchantment title
        if(enchantmentHolder != null){
            renderText(guiGraphics, x, y);
            renderIcon(guiGraphics, x, y);
        }
    }

    private void renderText(GuiGraphics guiGraphics, int x, int y) {
        final int padding = 4;
        guiGraphics.pose().pushPose();

        float scale = 1.0f;
        Component enchantmentTitle = enchantmentHolder.value().description();
        if(enchantmentTitle.getString().length() > 15) {
            scale = 0.75f;
            guiGraphics.pose().scale(scale, scale, scale);
        }

        int scaledX = (int)((x + padding) / scale);
        int scaledY = (int)((y + padding) / scale);

        //Create text with styling
        boolean isUnlocked = bookTab.screen.getMenu().isEnchantmentUnlocked(enchantmentHolder);
        Component enchantmentTitleStyled;
        if(ServerConfig.isObfuscateLockedEnchantments() && !isUnlocked) {
            enchantmentTitleStyled = ImmersiveEnchanting.getAltFont(enchantmentTitle.copy().withStyle(ChatFormatting.GRAY));
        } else {
            enchantmentTitleStyled = enchantmentTitle.copy().withStyle(ChatFormatting.WHITE);

        }


        guiGraphics.drawString(bookTab.screen.getMinecraft().font,
                enchantmentTitleStyled,
                scaledX,
                scaledY,
                0xFFFFFF);
        guiGraphics.pose().popPose();
    }

    private void renderIcon(GuiGraphics guiGraphics, int x, int y) {
        int xPos;
        if(bookTab.screen.getMenu().isEnchantmentUnlocked(enchantmentHolder)) {

            ItemStack icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);

            xPos = 91;
            guiGraphics.renderItem(
                    icon,
                    x+xPos,
                    y+2
            );

        } else {
            final int spriteSize = 19;
            xPos = 89;
            guiGraphics.blit(
                    Sprite.UNKNOWN.get(),
                    x+xPos,
                    y,
                    0f, 0f, spriteSize, spriteSize,
                    spriteSize, spriteSize
            );
        }
    }
}
