package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public enum GiveRandomAncientBookCommand implements ModCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ModCommands.simpleEntityArgumentCommand(
                        "giveRandomAncientBook",
                        Permissions.COMMANDS_ADMIN,
                        EntityArgument.players(),
                        context -> {
                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
                            for(ServerPlayer player : players) {
                                ItemStack ancientBook = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
                                EnchantmentUtil.setStoredEnchantment(
                                        ancientBook,
                                        CostRegistry.server().getRandomEnchantment(context.getSource().getLevel().getRandom()),
                                        context.getSource().getLevel());
                                player.getInventory().add(ancientBook);
                            }

                            context.getSource().sendSuccess(
                                    () -> Component.translatable("immersiveenchanting.command.give_random_ancient_book"), false);

                            return 1;
                        }
                )
        );
    }
}
