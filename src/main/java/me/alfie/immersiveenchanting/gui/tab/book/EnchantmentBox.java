package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.EnchantingTableLayout;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentTooltipColors;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

/**
 * Represents a single enchantment entry in the book tab UI.
 <P>
 * <p>Each box displays:</p>
 <P>
 * <ul>
 *     <li>A background indicating locked/unlocked state</li>
 *     <li>The enchantment name (optionally obfuscated)</li>
 *     <li>An icon or locked indicator</li>
 * </ul>
 <P>
 * <p>If the enchantment holder is {@code null}, an empty placeholder box is rendered.</p>
 */
public class EnchantmentBox {

    private static final float ICON_X_RATIO = 91f / EnchantingTableLayout.BOOK_BOX_SOURCE_WIDTH;
    private static final float LOCKED_ICON_X_RATIO = 89f / EnchantingTableLayout.BOOK_BOX_SOURCE_WIDTH;

    public final BookTab bookTab;
    private final Holder<Enchantment> enchantmentHolder;

    /**
     * Creates a new enchantment box for rendering.
     <P>
     * @param bookTab the parent {@link BookTab}
     * @param enchantmentHolder the enchantment to display, or {@code null} for an empty slot
     */
    public EnchantmentBox(BookTab bookTab, @Nullable Holder<Enchantment> enchantmentHolder) {
        this.bookTab = bookTab;
        this.enchantmentHolder = enchantmentHolder;
    }

    public void render(GuiGraphicsX gx, int x, int y) {
        Sprite boxSprite = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder) ?
                Sprite.ENCHANTMENT_BOX_UNLOCKED : Sprite.ENCHANTMENT_BOX_LOCKED;

        GuiGraphicsApi.blit(
                gx, boxSprite.id(),
                x, y,
                EnchantingTableLayout.BOOK_LIST_WIDTH,
                EnchantingTableLayout.BOOK_BOX_HEIGHT
        );

        if(enchantmentHolder != null){
            renderText(gx, x, y);
            renderIcon(gx, x, y);
        }
    }


    private void renderText(GuiGraphicsX gx, int x, int y) {
        Font font = bookTab.screen().getFont();
        int padding = EnchantingTableLayout.BOOK_TITLE_PADDING;
        int iconX = x + Math.round(EnchantingTableLayout.BOOK_LIST_WIDTH * ICON_X_RATIO);
        int maxTextWidth = Math.max(1, iconX - x - padding * 2);

        Component enchantmentName = enchantmentHolder.value().description();
        boolean isAvailable = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder);
        Component enchantmentTitle;
        if(!isAvailable && ServerConfig.isObfuscateLockedEnchantments()) {
            enchantmentTitle = ImmersiveEnchanting.styleWithAltFont(
                    enchantmentName.copy().withStyle(ChatFormatting.GRAY));
        } else {
            enchantmentTitle = EnchantmentTooltipColors.styleEnchantmentName(
                    enchantmentHolder,
                    enchantmentName.copy().withStyle(ChatFormatting.WHITE)
            );
        }

        int textWidth = font.width(enchantmentTitle);
        float scale = 1.0f;
        if(textWidth > maxTextWidth) {
            scale = Math.max(
                    EnchantingTableLayout.BOOK_TITLE_MIN_SCALE,
                    (float) maxTextWidth / (float) textWidth
            );
        }

        gx.graphics().pose().pushPose();
        float textX = x + padding;
        float textY = y + (EnchantingTableLayout.BOOK_BOX_HEIGHT - font.lineHeight * scale) / 2f;
        gx.graphics().pose().translate(textX, textY, 0);
        gx.graphics().pose().scale(scale, scale, 1);

        GuiGraphicsApi.text(gx, font, enchantmentTitle, 0, 0, true);
        gx.graphics().pose().popPose();
    }

    private void renderIcon(GuiGraphicsX gx, int x, int y) {
        int iconX = x + Math.round(EnchantingTableLayout.BOOK_LIST_WIDTH * ICON_X_RATIO);
        int iconY = y + (EnchantingTableLayout.BOOK_BOX_HEIGHT - 16) / 2;

        if(bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder)) {
            ResourceId icon = EnchantmentTextureHelper.getTexture(EnchantmentUtil.toId(enchantmentHolder));

            GuiGraphicsApi.blit(
                    gx,
                    icon,
                    iconX, iconY,
                    16, 16
            );

        } else {
            int lockedX = x + Math.round(EnchantingTableLayout.BOOK_LIST_WIDTH * LOCKED_ICON_X_RATIO);
            int lockedY = y + (EnchantingTableLayout.BOOK_BOX_HEIGHT - Sprite.LOCKED_ENCHANTMENT.height()) / 2;
            GuiGraphicsApi.blit(
                    gx,
                    Sprite.LOCKED_ENCHANTMENT.id(),
                    lockedX, lockedY,
                    Sprite.LOCKED_ENCHANTMENT.width(), Sprite.LOCKED_ENCHANTMENT.height()
            );
        }
    }
}
