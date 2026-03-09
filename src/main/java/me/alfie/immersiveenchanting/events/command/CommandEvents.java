package me.alfie.immersiveenchanting.events.command;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.command.*;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

public class CommandEvents {

    /**
     * Register mod commands.
     * @param event
     */
    @SubscribeEvent
    public static void onRegisterModCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        List<ImmersiveEnchantingCommand> commands = List.of(
                DisabledEnchantmentsCommand.COMMAND,
                EnabledEnchantmentsCommand.COMMAND,
                GiveRandomBookCommand.COMMAND,
                GenerateEmptyDatapack.COMMAND
        );

        for(ImmersiveEnchantingCommand command : commands) {
            command.register(dispatcher);
        }
    }

}
