package me.alfie.immersiveenchanting.gui;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.client.ModKeyMappings;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Small help marker in the viewport corner explaining the hold-to-filter shortcut.
 */
public final class ModFilterHelpHint {

    private static final int[] CIRCLE_ROW_INSETS = {3, 1, 0, 0, 0, 0, 0, 0, 1, 3};
    private static final int WHITE = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF000000;
    /** 5×7 pixel `?`; about 20% larger than the previous scaled glyph, without filtering. */
    private static final int[][] QUESTION = {
            {0, 1, 1, 1, 0},
            {1, 0, 0, 0, 1},
            {0, 0, 0, 0, 1},
            {0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0}
    };

    private final EnchantingTableScreen screen;

    public ModFilterHelpHint(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public void render(GuiGraphicsX gx, MousePos mousePos) {
        int x = screen.getGuiLeft() + EnchantingTableLayout.MOD_FILTER_HELP_INSET;
        int y = screen.getGuiTop() + EnchantingTableLayout.MOD_FILTER_HELP_INSET;
        int size = EnchantingTableLayout.MOD_FILTER_HELP_SIZE;
        ResourceLocation texture = Sprite.ENCHANTING_TABLE_GUI.id().mc();

        drawCircle(gx.graphics(), texture, x, y, size);
        drawQuestionMark(gx.graphics(), x, y, size);

        if (mousePos.isOver(x, y, size, size)) {
            screen.requestTabTooltip(ModKeyMappings.modFilterTooltip());
        }
    }

    private static void drawCircle(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        int tex = EnchantingTableLayout.TEXTURE_SIZE;
        int blackU = EnchantingTableLayout.TEXTURE_FRAME_BLACK_U;
        int blackV = EnchantingTableLayout.TEXTURE_FRAME_BLACK_V;
        int fillU = EnchantingTableLayout.TEXTURE_TAB_FILL_U;
        int fillV = EnchantingTableLayout.TEXTURE_TAB_FILL_V;

        for (int row = 0; row < CIRCLE_ROW_INSETS.length; row++) {
            int inset = CIRCLE_ROW_INSETS[row];
            int width = size - inset * 2;
            if (width <= 0) {
                continue;
            }
            blitPixel(graphics, texture, x + inset, y + row, width, 1, blackU, blackV, tex);
        }

        for (int row = 1; row < CIRCLE_ROW_INSETS.length - 1; row++) {
            int inset = CIRCLE_ROW_INSETS[row] + 1;
            int width = size - inset * 2;
            if (width <= 0) {
                continue;
            }
            blitPixel(graphics, texture, x + inset, y + row, width, 1, fillU, fillV, tex);
        }
    }

    private static void drawQuestionMark(GuiGraphics graphics, int x, int y, int size) {
        int glyphW = QUESTION[0].length;
        int glyphH = QUESTION.length;
        int originX = x + (size - glyphW) / 2;
        int originY = y + (size - glyphH) / 2;
        plotQuestion(graphics, originX + 1, originY + 1, SHADOW);
        plotQuestion(graphics, originX, originY, WHITE);
    }

    private static void plotQuestion(GuiGraphics graphics, int originX, int originY, int color) {
        for (int row = 0; row < QUESTION.length; row++) {
            for (int col = 0; col < QUESTION[row].length; col++) {
                if (QUESTION[row][col] != 0) {
                    graphics.fill(originX + col, originY + row, originX + col + 1, originY + row + 1, color);
                }
            }
        }
    }

    private static void blitPixel(
            GuiGraphics graphics, ResourceLocation texture,
            int x, int y, int w, int h, int u, int v, int tex
    ) {
        graphics.blit(texture, x, y, w, h, (float) u, (float) v, 1, 1, tex, tex);
    }
}
