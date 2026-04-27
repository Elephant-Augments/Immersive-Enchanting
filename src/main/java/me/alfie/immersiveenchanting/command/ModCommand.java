package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public interface ModCommand {

    void register(CommandDispatcher<CommandSourceStack> dispatcher);
}
