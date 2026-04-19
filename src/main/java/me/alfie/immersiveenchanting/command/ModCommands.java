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
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionSet;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

public class ModCommands {

    public static void registerCommands(RegisterCommandsEvent event) {
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

    /**
     * Creates a simple command under this mod's root literal with a permission requirement
     * and a single execution handler.
     *
     * <p>The resulting command structure is:
     * <pre>
     * <code>/modid commandName</code>
     * </pre>
     *
     * <p>The command will only be available to sources whose {@link PermissionSet}
     * contains the specified {@link Permission}. This uses the new permission system
     * via {@link PermissionCheck.Require}.
     *
     * @param commandName the literal name of the subcommand (e.g. "reload", "list")
     * @param permission  the {@link Permission} required to execute the command
     * @param executor    the command execution logic, returning a result integer
     *
     * @return a {@link LiteralArgumentBuilder} representing the fully configured command,
     *         ready to be registered with the {@link CommandDispatcher}
     *
     * @implNote This helper attaches the command under {@code ImmersiveEnchanting.MODID}
     *           as the root literal.
     *
     * @see Commands#literal(String)
     * @see Commands#hasPermission(PermissionCheck)
     * @see PermissionCheck.Require
     */
    public static LiteralArgumentBuilder<CommandSourceStack> simpleCommand(String commandName,
                                                                           Permission permission,
                                                                           Command<CommandSourceStack> executor) {
        return Commands.literal(ImmersiveEnchanting.MODID).then(Commands.literal(commandName)
                .requires(Commands.hasPermission(new PermissionCheck.Require(permission)))
                .executes(executor));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> simpleEntityArgumentCommand(String commandName,
                                                                                   Permission permission,
                                                                                   EntityArgument entityArgument,
                                                                                   Command<CommandSourceStack> executor) {
        return Commands.literal(ImmersiveEnchanting.MODID).then(Commands.literal(commandName)
                .requires(Commands.hasPermission(new PermissionCheck.Require(permission)))
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
