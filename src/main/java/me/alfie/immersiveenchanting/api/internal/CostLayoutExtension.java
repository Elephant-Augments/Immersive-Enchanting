package me.alfie.immersiveenchanting.api.internal;

import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.datapack.cost.EnchantmentCost;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNode;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CostLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip) {
        //Only apply for EnchantingNodeTooltips
        if(parentTooltip instanceof EnchantingNodeTooltip enchantingNodeTooltip) {
            if (enchantingNodeTooltip.node instanceof EnchantingNode enchantingNode) {
                enchantingNodeTooltip.setCurrentRenderedCost(
                        EnchantingNodeTooltip.getCycledElement(enchantingNodeTooltip.getValidCosts(), ClientConfig.getItemCarouselSpeed()));
                ItemStack stackToRender = enchantingNodeTooltip.getCurrentRenderedCost().asItemStack();

                CostEntry renderedCost = enchantingNodeTooltip.getCurrentRenderedCost();

                if(renderedCost.getCostItemTag().isPresent()) {
                    String itemTag = renderedCost.getCostItemTag().get().itemTag();
                    enchantingNodeTooltip.stackDescriptionComponents.set(0, Component.translatable("gui.immersiveenchanting.accepts_any_tag", itemTag)
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }

                //Cost
                if(!enchantingNode.isObtained() && enchantingNode.isBranchUnlocked) {
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
                            if (stackToRender.is(Items.AIR) || stackToRender.isEmpty()) {
                                graphics.drawString(
                                        Minecraft.getInstance().font,
                                        Component.translatable("gui.immersiveenchanting.cost_free"),
                                        lineX,
                                        lineY,
                                        ChatFormatting.DARK_AQUA.getColor()
                                );
                            } else {
                                Vector2i costStackPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);
                                enchantingNodeTooltip.setCostStackPos(costStackPos.x, costStackPos.y);
                                graphics.renderItem(
                                        stackToRender,
                                        costStackPos.x,
                                        costStackPos.y);
                                graphics.renderItemDecorations(Minecraft.getInstance().font,
                                        stackToRender,
                                        costStackPos.x,
                                        costStackPos.y);
                            }
                        }


                        @Override
                        public @NotNull Component getText() {
                            Component label;
                            label = Component.translatable("gui.immersiveenchanting.cost").withStyle(ChatFormatting.GRAY);
                            return label;
                        }
                    });
                }

                //Locked branch
                if(!enchantingNode.isBranchUnlocked) {
                    description.insertLine(0, new DescriptionLine() {
                        @Override
                        public void draw(GuiGraphics graphics, int lineX, int lineY) {
                            //Draw label
                            graphics.drawString(Minecraft.getInstance().font,
                                    getText(),
                                    lineX,
                                    lineY + 4, //Offset to centre text with cost stack
                                    0xFFFFFF);
                        }

                        @Override
                        public @NotNull Component getText() {
                            Component label = Component.translatable("gui.immersiveenchanting.locked_enchantment_hint").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.GRAY);
                            return label;
                        }
                    });
                }

                //Equipped
                if(enchantingNode.isObtained()) {
                    description.insertLine(0, new DescriptionLine() {
                        @Override
                        public void draw(GuiGraphics graphics, int lineX, int lineY) {
                            //Draw label
                            graphics.drawString(Minecraft.getInstance().font,
                                    getText(),
                                    lineX,
                                    lineY + 4, //Offset to centre text with cost stack
                                    0xFFFFFF);
                        }

                        @Override
                        public @NotNull Component getText() {
                            Component label = Component.translatable("gui.immersiveenchanting.equipped").withStyle(ChatFormatting.LIGHT_PURPLE);
                            return label;
                        }
                    });
                    int enchantmentLevel = enchantingNode.getEnchantmentLevel();
                    int highestUnlockedLevel = enchantingNodeTooltip.screen.getMenu().getToolSlotItem().getEnchantmentLevel(enchantingNode.getEnchantmentHolder());

                    //Removal
                    if (enchantmentLevel == highestUnlockedLevel && ServerConfig.isEnchantmentRemovalAllowed()) {
                        description.insertLine(1, new DescriptionLine() {
                            @Override
                            public void draw(GuiGraphics graphics, int lineX, int lineY) {
                                graphics.drawString(Minecraft.getInstance().font,
                                        getText(),
                                        lineX,
                                        lineY + 4, //Offset to centre text with cost stack
                                        0xFFFFFF);
                            }

                            @Override
                            public @NotNull Component getText() {
                                return Component.translatable("gui.immersiveenchanting.hold_to_remove").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                            }
                        });

                        if(enchantingNodeTooltip.screen.getMouseHeldTime() > 0 ) {
                            description.insertLine(2, new DescriptionLine() {
                                @Override
                                public void draw(GuiGraphics graphics, int lineX, int lineY) {
                                    long heldTime = enchantingNodeTooltip.screen.getMouseHeldTime();
                                    long threshold = enchantingNodeTooltip.screen.HOLD_THRESHOLD;
                                    float progress = Math.min(1f, (float) heldTime / threshold);

                                    int bars = (int) (heldTime / (threshold/15));


                                    for (int i = enchantingNodeTooltip.lastBars; i < bars; i++) {
                                        float pitch = 2f - progress;
                                        pitch = Math.max(pitch, 1f);

                                        enchantingNodeTooltip.screen.player.playSound(
                                                SoundEvents.EXPERIENCE_ORB_PICKUP, 0.3f, pitch
                                        );
                                    }
                                    enchantingNodeTooltip.lastBars = bars;

                                    String barText = "";
                                    for (int i = 0; i < bars; i++) {
                                        barText += "|";
                                    }

                                    Component barComponent = Component.literal(barText).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
                                    graphics.drawString(Minecraft.getInstance().font,
                                            barComponent,
                                            lineX,
                                            lineY + 4, //Offset to centre text with cost stack
                                            0xFFFFFF);
                                }

                                @Override
                                public @NotNull Component getText() {
                                    return Component.empty();
                                }
                            });
                        }
                    }
                }

                //XP rendering
                if(renderedCost.xpLevels() > 0 && enchantingNode.isBranchUnlocked && !enchantingNode.isObtained()) {
                    description.insertLine(2, new DescriptionLine() {
                        @Override
                        public void draw(GuiGraphics graphics, int lineX, int lineY) {
                            //Draw label
                            graphics.drawString(Minecraft.getInstance().font,
                                    getText(),
                                    lineX,
                                    lineY + 4, //Offset to centre text with cost stack
                                    0xFFFFFF);

                            Vector2i levelLabelPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);

                            //Draw XP sprite
                            graphics.blit(
                                    EnchantingTableScreen.XP_LEVEL_SPRITE,
                                    levelLabelPos.x,
                                    lineY,
                                    0, 0, 16, 16, 16, 16);

                            //Draw XP level number
                            graphics.drawString(
                                    Minecraft.getInstance().font,
                                    Component.literal(String.valueOf(renderedCost.xpLevels())),
                                    levelLabelPos.x + 12,
                                    lineY + 4,
                                    0xC8FF8F
                            );
                        }

                        @Override
                        public @NotNull Component getText() {
                            Component label = Component.translatable("gui.immersiveenchanting.cost.xp")
                                    .withStyle(ChatFormatting.GRAY);
                            return label;
                        }
                    });
                }
            }
        }
    }


}
