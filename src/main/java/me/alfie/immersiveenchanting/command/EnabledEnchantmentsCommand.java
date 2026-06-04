package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum EnabledEnchantmentsCommand implements ModCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ModCommands.simpleCommand(
                        "enabledEnchantments",
                        2,
                        context -> {
                            List<Holder<Enchantment>> datapackEnchantments =
                                    CostRegistry.server().getAllEnabledEnchantmentHolders();

                            String enabled = datapackEnchantments.stream()
                                    .map(Holder::unwrapKey)
                                    .flatMap(Optional::stream)
                                    .map(ResourceKey::location)
                                    .map(ResourceLocation::toString)
                                    .collect(Collectors.joining(", "));
                            Component enabledEnchantments = Component.literal(enabled).withStyle(ChatFormatting.GREEN);

                            context.getSource().sendSuccess(
                                    () -> Component.translatable("immersiveenchanting.command.enabled_enchantments", enabledEnchantments)
                                            .withStyle(ChatFormatting.WHITE), false);


                            return 1;
                        }
                )
        );
    }
}
