package me.alfie.immersiveenchanting.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public enum GenerateEmptyDatapack implements ImmersiveEnchantingCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("immersiveenchanting")
                        .then(Commands.literal("generateEmptyDatapack")
                                .executes(context -> {
                                    Level level = context.getSource().getLevel();
                                    File worldFolder = level.getServer().getWorldPath(LevelResource.GENERATED_DIR).toFile();
                                    File rootFolder = new File(worldFolder, "immersiveenchanting-generateddatapack");

                                    if(rootFolder.exists()) {
                                        try {
                                            FileUtils.deleteDirectory(rootFolder);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    rootFolder.mkdirs();

                                    File enchantmentCostFolder = new File(rootFolder, "enchantment_costs");
                                    if(!enchantmentCostFolder.exists()) {
                                        enchantmentCostFolder.mkdirs();
                                    }

                                    List<Holder.Reference<Enchantment>> allEnchantments = EnchantmentUtil.getAllEnchantments(level, false);

                                    for(Holder<Enchantment> enchantmentHolder : allEnchantments) {
                                        String id = enchantmentHolder.getRegisteredName();

                                        String[] parts = id.split(":", 2);

                                        String namespace = parts[0];
                                        String enchantName = parts[1];

                                        //Try to make namespace folder if doesn't exist
                                        File namespaceFolder = new File(enchantmentCostFolder, namespace);
                                        if(!namespaceFolder.exists()) {
                                            namespaceFolder.mkdirs();
                                        }

                                        File jsonFile = new File(namespaceFolder, enchantName + ".json");

                                        //Build JSON
                                        JsonObject level1 = new JsonObject();
                                        level1.addProperty("item", "minecraft:air");
                                        level1.addProperty("amount", 0);
                                        level1.addProperty("xp_levels", 0);
                                        JsonObject levels = new JsonObject();
                                        levels.add("1", level1);
                                        JsonObject root = new JsonObject();
                                        root.addProperty("enabled", true);
                                        root.add("levels", levels);

                                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                        try(FileWriter writer = new FileWriter(jsonFile)) {
                                            gson.toJson(root, writer);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Component message;
                                    if(level.isClientSide) {
                                        message = Component.literal("Success! Click here to open the generated folder.")
                                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, rootFolder.getAbsolutePath())))
                                                .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GREEN);
                                    } else {
                                        //Opening files is blocked server side.
                                        message = Component.literal("Success! Click to copy the generated folder path.")
                                                .withStyle(style -> style
                                                        .withColor(ChatFormatting.GREEN)
                                                        .withUnderlined(true)
                                                        .withClickEvent(new ClickEvent(
                                                                ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                                rootFolder.getAbsolutePath())));
                                    }
                                    Component note = Component.literal("Note: You will need to create a pack.mcmeta file!");

                                    context.getSource().sendSuccess(() -> message, false);
                                    context.getSource().sendSuccess(() -> note, false);

                                    return 1;
                                })
                        )
        );
    }

}
