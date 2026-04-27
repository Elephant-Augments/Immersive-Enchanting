package me.alfie.immersiveenchanting.command;

import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                            List<Holder.Reference<Enchantment>> registeredEnchantments =
                                    EnchantmentUtil.getAllRegisteredEnchantments(context.getSource().registryAccess());

                            List<Holder<Enchantment>> datapackEnchantments =
                                    CostRegistry.server().getAllEnchantmentHolders();

                            Set<Holder<Enchantment>> datapackSet = new HashSet<>(datapackEnchantments);

                            List<Holder<Enchantment>> result = registeredEnchantments.stream()
                                    .filter(e -> !datapackSet.contains(e))
                                    .map(e -> (Holder<Enchantment>) e)
                                    .toList();

                            String disabled = result.stream()
                                    .map(Holder::unwrapKey)
                                    .flatMap(Optional::stream)
                                    .map(ResourceKey::location)
                                    .map(ResourceLocation::toString)
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
