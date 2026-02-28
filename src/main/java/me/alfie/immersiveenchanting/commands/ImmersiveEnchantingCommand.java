package me.alfie.immersiveenchanting.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public interface ImmersiveEnchantingCommand {
    void register(CommandDispatcher<CommandSourceStack> dispatcher);
}
