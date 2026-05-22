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

/**
 * Represents a single enchantment entry in the book tab UI.
 * <P>
 * <p>Each box displays:</p>
 * <P>
 * <ul>
 *     <li>A background indicating locked/unlocked state</li>
 *     <li>The enchantment name (optionally obfuscated)</li>
 *     <li>An icon or locked indicator</li>
 * </ul>
 * <P>
 * <p>If the enchantment holder is {@code null}, an empty placeholder box is rendered.</p>
 */
public class EnchantmentBox {

    public final BookTab bookTab;
    private final Holder<Enchantment> enchantmentHolder;

    /**
     * Creates a new enchantment box for rendering.
     * <p>
     *
     * @param bookTab           the parent {@link BookTab}
     * @param enchantmentHolder the enchantment to display, or {@code null} for an empty slot
     */
    public EnchantmentBox(BookTab bookTab, @Nullable Holder<Enchantment> enchantmentHolder) {
        this.bookTab = bookTab;
        this.enchantmentHolder = enchantmentHolder;
    }

    /**
     * Renders the enchantment box at the given position.
     * <P>
     * <p>This includes:</p>
     * <p>
     * <ul>
     *     <li>Background sprite (locked or unlocked)</li>
     *     <li>Enchantment text (if present)</li>
     *     <li>Icon or locked indicator</li>
     * </ul>
     * <p>
     *
     * @param graphics the GUI rendering context
     * @param x        the X position of the box
     * @param y        the Y position of the box
     */
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

        if (enchantmentHolder != null) {
            renderText(graphics, x, y);
            renderIcon(graphics, x, y);
        }
    }

    /**
     * Renders the enchantment name text inside the box.
     * <P>
     * <p>Text is scaled down if it exceeds a certain length.</p>
     * <P>
     * <p>If the enchantment is locked and obfuscation is enabled,
     * the text is styled with an alternate font.</p>
     * <p>
     *
     * @param graphics the GUI rendering context
     * @param x        the X position of the box
     * @param y        the Y position of the box
     */
    private void renderText(GuiGraphics graphics, int x, int y) {
        final int padding = 4;
        graphics.pose().pushPose();

        float scale = 1.0f;

        Component enchantmentName = enchantmentHolder.value().description();
        if (enchantmentName.getString().length() > 15) {
            scale = 0.75f;
            graphics.pose().scale(scale, scale, 1);
        }

        int scaledX = (int) ((x + padding) / scale);
        int scaledY = (int) ((y + padding) / scale);

        boolean isAvailable = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder);
        Component enchantmentTitle;
        if (!isAvailable && ServerConfig.isObfuscateLockedEnchantments()) {
            enchantmentTitle = ImmersiveEnchanting.styleWithAltFont(
                    enchantmentName.copy().withStyle(ChatFormatting.GRAY));
        } else {
            enchantmentTitle = enchantmentName.copy().withStyle(ChatFormatting.WHITE);
        }

        graphics.drawString(Minecraft.getInstance().font, enchantmentTitle, scaledX, scaledY, Color.WHITE.getRGB());
        graphics.pose().popPose();
    }

    /**
     * Renders the icon associated with the enchantment.
     * <P>
     * <p>If the enchantment is unlocked, a custom texture is displayed.</p>
     * <P>
     * <p>If locked, a default locked icon is rendered instead.</p>
     * <p>
     *
     * @param graphics the GUI rendering context
     * @param x        the X position of the box
     * @param y        the Y position of the box
     */
    private void renderIcon(GuiGraphics graphics, int x, int y) {

        if (bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder)) {
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
