package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

/**
 * A clickable checkbox UI element used to enable or disable a {@link BookFilters} option
 * within the {@link BookTab}.
 *
 * <p>This component:
 * <ul>
 *     <li>Visually represents filter state (enabled/disabled)</li>
 *     <li>Handles mouse interaction</li>
 *     <li>Notifies the parent {@link BookTab} to reset scrolling when toggled</li>
 * </ul>
 */
public class FilterCheckbox implements ScreenEventListener {

    protected final BookFilters filterType;
    private final BookTab bookTab;
    private boolean enabled = true;
    private int x;
    private int y;

    public FilterCheckbox(BookTab bookTab, BookFilters filter) {
        filterType = filter;
        this.bookTab = bookTab;
    }

    /**
     * Renders the checkbox and its label.
     *
     * <p>Displays either an "on" or "off" sprite depending on state,
     * and changes the cursor when hovered.</p>
     *
     * @param graphics Rendering context
     * @param mouseX   Current mouse X
     * @param mouseY   Current mouse Y
     */
    public void render(GuiGraphics graphics, double mouseX, double mouseY) {
        Sprite sprite = isEnabled() ? Sprite.CHECKBOX_ON : Sprite.CHECKBOX_OFF;

        int x = bookTab.screen().getGuiLeft() + this.x;
        int y = bookTab.screen().getGuiTop() + this.y;
        graphics.blit(
                sprite.id(),
                x,
                y,
                0f, 0f,
                sprite.width(), sprite.height(),
                sprite.width(), sprite.height()
        );

        graphics.drawString(Minecraft.getInstance().font, filterType.getLabel(), x + 20, y, Color.WHITE.getRGB());
    }

    /**
     * @return {@code true} if this filter is currently enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled state of this filter.
     *
     * @param enabled The new state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Handles mouse click interaction.
     *
     * <p>If clicked while hovered:
     * <ul>
     *     <li>Toggles the filter</li>
     *     <li>Plays a UI sound</li>
     * </ul>
     *
     * @param mouse Mouse event
     * @return {@code true} if the click was handled
     */
    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            FxHelper.playGenericUISound(bookTab.screen().player());
            toggleEnabled();
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);
    }

    /**
     * Checks whether the mouse is hovering over this checkbox.
     *
     * @param mouseX Current mouse X
     * @param mouseY Current mouse Y
     * @return {@code true} if the mouse is within bounds
     */
    private boolean isMouseOver(double mouseX, double mouseY) {
        return bookTab.screen().isMouseOver(
                bookTab.screen().getGuiLeft() + x, bookTab.screen().getGuiTop() + y,
                Sprite.CHECKBOX_ON.width(), Sprite.CHECKBOX_ON.height(),
                mouseX, mouseY);
    }

    /**
     * Toggles the enabled state of this filter.
     *
     * <p>Also resets the scrollbar position in the parent {@link BookTab}
     * to ensure consistent rendering after filtering changes.</p>
     */
    public void toggleEnabled() {
        this.enabled = !this.enabled;
        bookTab.scrollbar().resetScrollIndex();
    }

    /**
     * @return X position relative to the GUI
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the X position relative to the GUI.
     *
     * @param x New X position
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Y position relative to the GUI
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the Y position relative to the GUI.
     *
     * @param y New Y position
     */
    public void setY(int y) {
        this.y = y;
    }
}
