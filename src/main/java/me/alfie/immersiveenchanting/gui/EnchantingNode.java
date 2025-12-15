package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.enums.EnchantingNodeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

public class EnchantingNode {
    static final int width = 26; //Texture size
    static final int height = 26; //Texture size
    public static float globalScale = 1.0f;
    final boolean isBranchUnlocked;
    private final EnchantingNodeType node_type;
    private final ResourceLocation icon_texture;
    private final int enchantmentLevel;
    private final ResourceKey<Enchantment> enchantment;
    private final Holder<Enchantment> enchantmentHolder;
    private final ResourceLocation LOCKED_ICON = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/enchantment_icons/locked_enchantment.png");
    private int x, y;
    private boolean obtained;
    private float scale = 1;

    public EnchantingNode(EnchantingNodeType node_type, ResourceLocation icon_texture, int enchantmentLevel,
                          Holder<Enchantment> enchantmentHolder,
                          boolean isBranchUnlocked) {
        this.isBranchUnlocked = isBranchUnlocked;
        this.enchantmentLevel = enchantmentLevel;
        this.enchantmentHolder = enchantmentHolder;
        this.enchantment = enchantmentHolder.unwrapKey().get();

        if (isBranchUnlocked) {
            this.node_type = node_type;
            this.icon_texture = icon_texture;
        } else {
            this.node_type = EnchantingNodeType.LOCKED;
            //this.icon_texture = LOCKED_ICON;
            this.icon_texture = null;
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isInViewport(double scrollX, double scrollY, int viewportWidth, int viewportHeight) {
        return getX() + width >= scrollX &&
                getX() <= scrollX + viewportWidth &&
                getY() + height >= scrollY &&
                getY() <= scrollY + viewportHeight;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void render(GuiGraphics guiGraphics, EnchantingTableScreen screen) {
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.x - (int) screen.scrollX, this.y - (int) screen.scrollY, 0);
        guiGraphics.pose().scale(scale, scale, scale);

        guiGraphics.blit(
                this.getCurrentTexture(),
                0,
                0,
                0f, 0f,
                width, height,
                width, height
        );

        //Blit book texture
        if (icon_texture != null) {
            guiGraphics.blit(
                    icon_texture,
                    4,
                    4,
                    0f, 0f,
                    16, 16,
                    16, 16
            );
        }
        guiGraphics.pose().popPose();

    }

    public ResourceLocation getCurrentTexture() {
        return obtained ? node_type.getObtainedTexture() : node_type.getUnobtainedTexture();
    }

    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    public Holder<Enchantment> getEnchantmentHolder() {
        return enchantmentHolder;
    }

    public ResourceKey<Enchantment> getEnchantment() {
        return enchantment;
    }

    public boolean isObtained() {
        return this.obtained;
    }

    public void setObtained(boolean obtained) {
        this.obtained = obtained;
    }

    public Vector2i getRenderedPosition(EnchantingTableScreen screen) {
        int renderedX = this.x - (int) screen.scrollX;
        int renderedY = this.y - (int) screen.scrollY;
        return new Vector2i(renderedX, renderedY);
    }

    public Vector2i getViewportPosition(EnchantingTableScreen screen) {
        int screenX = this.x - (int) screen.scrollX;
        int screenY = this.y - (int) screen.scrollY;

        int viewportX = screenX - screen.canvasLeftPos;
        int viewportY = screenY - screen.canvasTopPos;

        return new Vector2i(viewportX, viewportY);
    }

}
