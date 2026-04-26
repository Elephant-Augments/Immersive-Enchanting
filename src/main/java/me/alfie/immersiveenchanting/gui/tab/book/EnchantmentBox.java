package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    public void render(GuiGraphics graphics, int x, int y) {
        Sprite boxSprite = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder) ?
                Sprite.ENCHANTMENT_BOX_UNLOCKED : Sprite.ENCHANTMENT_BOX_LOCKED;

        graphics.blit(
                boxSprite.id(),
                x,
                y,
                0f, 0f,
                boxSprite.width(), boxSprite.height(),
                boxSprite.width(), boxSprite.height()
        );

        if(enchantmentHolder != null){
            renderText(graphics, x, y);
            renderIcon(graphics, x, y);
        }
    }

    private void renderText(GuiGraphics graphics, int x, int y) {
        final int padding = 4;
        graphics.pose().pushPose();

        float scale = 1.0f;

        Component enchantmentName = enchantmentHolder.value().description();
        if(enchantmentName.getString().length() > 15) {
            scale = 0.75f;
            graphics.pose().scale(scale, scale, 0);
        }

        int scaledX = (int)((x + padding) / scale);
        int scaledY = (int)((y + padding) / scale);

        boolean isAvailable = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder);
        Component enchantmentTitle;
        if(!isAvailable && ServerConfig.isObfuscateLockedEnchantments()) {
            enchantmentTitle = ImmersiveEnchanting.styleWithAltFont(
                    enchantmentName.copy().withStyle(ChatFormatting.GRAY));
        } else {
            enchantmentTitle = enchantmentName.copy().withStyle(ChatFormatting.WHITE);
        }

        graphics.drawString(Minecraft.getInstance().font, enchantmentTitle, scaledX, scaledY, Color.WHITE.getRGB());
        graphics.pose().popPose();
    }

    private void renderIcon(GuiGraphics graphics, int x, int y) {

        if(bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder)) {
            ResourceLocation icon = EnchantmentTextureHelper.getTexture(enchantmentHolder.getKey().location());

            final int xPos = x + 91;
            final int yPos = y + 2;
            graphics.blit(
                icon,
                xPos, yPos,
                0f, 0f,
                16, 16,
                16, 16
            );

        } else {
            final int xPos = x + 89;
            graphics.blit(
                    Sprite.LOCKED_ENCHANTMENT.id(),
                    xPos, y,
                    0f, 0f,
                    Sprite.LOCKED_ENCHANTMENT.width(), Sprite.LOCKED_ENCHANTMENT.height(),
                    Sprite.LOCKED_ENCHANTMENT.width(), Sprite.LOCKED_ENCHANTMENT.height()
            );
        }
    }
}
