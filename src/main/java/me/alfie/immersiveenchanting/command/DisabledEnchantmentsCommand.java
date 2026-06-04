package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.stream.Collectors;

public enum DisabledEnchantmentsCommand implements ModCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ModCommands.simpleCommand(
                        "disabledEnchantments",
                        2,
                        context -> {
                            List<Holder<Enchantment>> registeredEnchantments =
                                    EnchantmentUtil.getAllEnchantmentsInRegistry(context.getSource().registryAccess());

                            List<Holder<Enchantment>> all =
                                    CostRegistry.server().getAllEnchantmentHolders();

                            List<Holder<Enchantment>> enabled =
                                    CostRegistry.server().getAllEnabledEnchantmentHolders();

                            //Disabled enchantments
                            List<Holder<Enchantment>> result = all.stream()
                                    .filter(holder -> !enabled.contains(holder))
                                    .toList();

                            String disabled = result.stream()
                                    .map(Holder::getRegisteredName)
                                    .collect(Collectors.joining(", "));
                            Component disabledEnchantments = Component.literal(disabled).withStyle(ChatFormatting.RED);

                            context.getSource().sendSuccess(
                                    () -> Component.translatable("immersiveenchanting.command.disabled_enchantments", disabledEnchantments)
                                            .withStyle(ChatFormatting.WHITE), false);

                            return 1;
                        }
                )
        );
    }
}
