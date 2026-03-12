package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public enum DisabledEnchantmentsCommand implements ImmersiveEnchantingCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersiveenchanting")
                        .then(Commands.literal("getDisabledEnchantments")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    List<String> strings = EnchantmentCostRegistry.getServerRegistry().getDisabledEnchantments();
                                    Collections.sort(strings);

                                    String disabled = String.join(", ", strings);
                                    context.getSource().sendSuccess(() -> Component.literal("Disabled enchantments: " + disabled).withStyle(ChatFormatting.RED), false);
                                    return 1;
                                })
                        )
                );
    }
}
