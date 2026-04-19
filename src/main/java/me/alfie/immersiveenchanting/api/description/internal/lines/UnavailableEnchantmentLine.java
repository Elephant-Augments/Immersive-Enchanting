package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record UnavailableEnchantmentLine(NodeTooltip tooltip) implements DescriptionLine {
    @Override
    public void render(GuiGraphicsExtractor graphics, int lineX, int lineY, double mouseX, double mouseY) {
        DescriptionHelper.text(graphics, getText(), lineX, lineY);
    }

    @Override
    public @NotNull Component getText() {
        return ImmersiveEnchanting.styleWithAltFont(
                Component.translatable("immersiveenchanting.tooltip.desc.unavailable_enchantment")
                        .withStyle(ChatFormatting.GRAY));
    }
}
