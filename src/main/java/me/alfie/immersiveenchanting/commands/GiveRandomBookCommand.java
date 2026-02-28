package me.alfie.immersiveenchanting.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.lootmodifier.AncientBookLootModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collection;

public enum GiveRandomBookCommand implements ImmersiveEnchantingCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersiveenchanting")
                        .then(Commands.literal("giveRandomBook")
                                // target argument
                                .then(Commands.argument("target", EntityArgument.players())
                                        .executes(context -> {
                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
                                            for (ServerPlayer player : players) {
                                                ItemStack book = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
                                                Holder<Enchantment> randomEnchantment =
                                                        AncientBookLootModifier.getRandomEnchantment(player.level(), player.getRandom());
                                                AncientBook.setStoredEnchantment(book, randomEnchantment);
                                                player.getInventory().add(book);
                                            }
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Gave random book to selected player(s)!"), true
                                            );
                                            return 1;
                                        })
                                )
                                // fallback executes when no target is provided
                                .executes(context -> {
                                    context.getSource().sendFailure(
                                            Component.literal("No target given!")
                                    );
                                    return 0;
                                })
                        )
        );
    }
}
