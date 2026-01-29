package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public final class TooltipDescriptionExtensions {
    private static final List<DescriptionLayoutExtension> EXTENSIONS = new ArrayList<>();

    /**
     * Register a new DescriptionLayoutExtension that will be rendered in EnchantingNodeTooltips.
     * @param extension
     */
    public static void register(DescriptionLayoutExtension extension) {
        EXTENSIONS.add(extension);
    }

    /**
     * Call extendLayout on all registered DescriptionLayoutExtensions
     * @param parentTooltip
     * @param descriptionLayout
     */
    public static void apply(EnchantingNodeTooltip parentTooltip,
                      DescriptionLayout descriptionLayout) {
        for(DescriptionLayoutExtension extension : EXTENSIONS) {
            try {
                extension.extendLayout(descriptionLayout, parentTooltip);
            } catch (Exception e) {
                ImmersiveEnchanting.LOGGER.error("Tooltip extension failed: {}", extension.getClass().getName(), e);
            }
        }
    }

    //Register internal description layouts.
    static {
        //Using the API hooks internally here to add text/custom rendering into the description box.
        //Draws the label: "Cost: " "Equipped" or "Unknown (obfuscated)" depending on the status of the EnchantingNode
        //Also renders the item cost next to it.
        TooltipDescriptionExtensions.register(new DescriptionLayoutExtension() {
            @Override
            public void extendLayout(DescriptionLayout description, EnchantingNodeTooltip parentTooltip) {
                description.insertLine(0, new DescriptionLine() {
                    @Override
                    public void draw(GuiGraphics graphics, int lineX, int lineY) {
                        //Draw label
                        graphics.drawString(Minecraft.getInstance().font,
                                getText(),
                                lineX,
                                lineY + 4, //Offset to centre text with cost stack
                                0xFFFFFF);

                        //Draw cost stack or "Free" if no item cost defined.
                        if (parentTooltip.node.isBranchUnlocked && !parentTooltip.node.isObtained()) {
                            ItemStack costStack = parentTooltip.getCostStack();
                            if(costStack.is(Items.AIR) || costStack.isEmpty()) {
                                graphics.drawString(
                                        Minecraft.getInstance().font,
                                        Component.translatable("gui.immersiveenchanting.cost_free"),
                                        lineX,
                                        lineY,
                                        ChatFormatting.DARK_AQUA.getColor()
                                );
                            } else {
                                Vector2i costStackPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);
                                parentTooltip.setCostStackPos(costStackPos.x, costStackPos.y);
                                graphics.renderItem(
                                        parentTooltip.getCostStack(),
                                        costStackPos.x,
                                        costStackPos.y);
                                graphics.renderItemDecorations(Minecraft.getInstance().font,
                                        parentTooltip.getCostStack(),
                                        costStackPos.x,
                                        costStackPos.y);
                            }
                        }
                    }

                    @Override
                    public @NotNull Component getText() {
                        Component label;
                        if (parentTooltip.node.isBranchUnlocked) {
                            label = parentTooltip.node.isObtained() ?
                                    Component.translatable("gui.immersiveenchanting.equipped").withStyle(ChatFormatting.LIGHT_PURPLE) :
                                    Component.translatable("gui.immersiveenchanting.cost").withStyle(ChatFormatting.GRAY);
                        } else {
                            label = Component.translatable("gui.immersiveenchanting.locked_enchantment_hint")
                                    .withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.GRAY);
                        }
                        return label;
                    }
                });
            }
        });
    }
}
