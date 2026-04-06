package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.NineSliceSprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class TooltipTitle extends NineSliceBox {

    private Component titleText;
    private final NodeTooltip nodeTooltip;

    public TooltipTitle(NodeTooltip nodeTooltip) {
        super(nodeTooltip.node().isState(NodeState.OBTAINED) ?
                NineSliceSprite.TOOLTIP_OBTAINED.id()
                : NineSliceSprite.TOOLTIP_UNOBTAINED.id());
        this.nodeTooltip = nodeTooltip;

        Component titleText = EnchantmentUtil.getEnchantmentTitleFromId(nodeTooltip.node().enchantmentId(),
                nodeTooltip.node().enchantmentLevel(),
                nodeTooltip.screen().registryAccess());
        setTitleText(titleText);
    }

    public void setTitleText(Component component) {
        component = component.copy().withStyle(ChatFormatting.WHITE);

        if(nodeTooltip.node().isState(NodeState.LOCKED)) {
            component = ImmersiveEnchanting.styleWithAltFont(component);
        }

        this.titleText = component.copy().withStyle(ChatFormatting.WHITE);
    }

    public Component getTitleText() {
        return titleText;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        super.render(graphics);
        graphics.text(Minecraft.getInstance().font, titleText, x(), y(), Color.WHITE.getRGB());
    }
}
