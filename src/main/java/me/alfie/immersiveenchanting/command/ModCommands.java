package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

public class ModCommands {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        List<ModCommand> commands = List.of(
                DisabledEnchantmentsCommand.COMMAND,
                EnabledEnchantmentsCommand.COMMAND,
                GiveRandomAncientBookCommand.COMMAND,
                GenerateEmptyDatapackCommand.COMMAND
        );

        for(ModCommand command : commands) {
            command.register(dispatcher);
        }

    }

    public static LiteralArgumentBuilder<CommandSourceStack> simpleCommand(String commandName,
                                                                           int permission,
                                                                           Command<CommandSourceStack> executor) {
        return Commands.literal(ImmersiveEnchanting.MODID).then(Commands.literal(commandName)
                .requires(source -> source.hasPermission(permission))
                .executes(executor));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> simpleEntityArgumentCommand(String commandName,
                                                                                   int permission,
                                                                                   EntityArgument entityArgument,
                                                                                   Command<CommandSourceStack> executor) {
        return Commands.literal(ImmersiveEnchanting.MODID).then(Commands.literal(commandName)
                .requires(source -> source.hasPermission(permission))
                .then(Commands.argument("target", entityArgument).executes(executor))
                .executes(context -> {
                    context.getSource().sendFailure(
                            Component.translatable("immersiveenchanting.command.no_target")
                                    .withStyle(ChatFormatting.RED)
                    );
                    return 0;
                }));
    }
}
