package me.alfie.immersiveenchanting.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public enum EnabledEnchantmentsCommand implements ImmersiveEnchantingCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersiveenchanting")
                        .then(Commands.literal("getEnabledEnchantments")
                                .executes(context -> {
                                    List<String> strings = EnchantmentCostRegistry.getServerRegistry().getEnabledEnchantments();
                                    Collections.sort(strings);

                                    String enabled = String.join(", ", strings);
                                    context.getSource().sendSuccess(() -> Component.literal("Enabled enchantments: " + enabled).withStyle(ChatFormatting.GREEN), false);
                                    return 1;
                                })
                        )
                );
    }
}
