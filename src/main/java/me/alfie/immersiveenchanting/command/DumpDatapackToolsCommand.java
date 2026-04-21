package me.alfie.immersiveenchanting.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public enum DumpDatapackToolsCommand implements ModCommand {
    COMMAND;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ModCommands.simpleCommand(
                        "dumpDatapackTools",
                        Permissions.COMMANDS_OWNER,
                        context -> {
                            RegistryAccess registryAccess = context.getSource().getServer().registryAccess();
                            ResourceManager resourceManager = context.getSource().getServer().getResourceManager();

                            Registry<Enchantment> enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
                            Registry<Item> itemRegistry = registryAccess.lookupOrThrow(Registries.ITEM);

                            generateFile(context.getSource().getLevel(),
                                    getRegistryContents(itemRegistry),
                                    getRegistryContents(enchantmentRegistry),
                                    getLootTables(resourceManager));
                            return 1;
                        }
                )
        );
    }

    private static void generateFile(ServerLevel level,
                                     List<Identifier> items,
                                     List<Identifier> enchantments,
                                     List<Identifier> lootTables) {
        File worldFolder = level.getServer().getWorldPath(LevelResource.GENERATED_DIR).toFile();
        File file = new File(worldFolder, "datapacktool.datapacktool");

        try {
            file.getParentFile().mkdirs();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Map<String, List<String>> data = new HashMap<>();

            data.put("items", items.stream().map(Identifier::toString).toList());
            data.put("enchantments", enchantments.stream().map(Identifier::toString).toList());
            data.put("loot_tables", lootTables.stream().map(Identifier::toString).toList());

            String json = gson.toJson(data);

            Files.writeString(file.toPath(), json);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> List<Identifier> getRegistryContents(Registry<T> registry) {
        List<Identifier> result = new ArrayList<>();
        for(T entry : registry) {
            Identifier id = registry.getKey(entry);
            if(id != null) result.add(id);
        }

        return result;
    }

    private static List<Identifier> getLootTables(ResourceManager resourceManager) {
        return resourceManager.listResources("loot_table", identifier -> true).keySet().stream().toList();
    }
}
