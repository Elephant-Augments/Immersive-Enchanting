package me.alfie.immersiveenchanting.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public enum GenerateEmptyDatapackCommand implements ModCommand {
    COMMAND;

    public static final double PACK_FORMAT = 101.1;

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ModCommands.simpleCommand(
                        "generateEmptyDatapack",
                        4,
                        context -> {
                            Level level = context.getSource().getLevel();
                            File worldFolder = level.getServer().getWorldPath(LevelResource.GENERATED_DIR).toFile();
                            File rootDir = new File(worldFolder, ImmersiveEnchanting.MODID + "_GENERATED_DATAPACK");

                            //Create folder structure
                            if (rootDir.exists()) {
                                try {
                                    FileUtils.deleteDirectory(rootDir);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            rootDir.mkdirs();
                            File dataDir = new File(rootDir, "data");
                            dataDir.mkdirs();
                            File immersiveEnchantingDir = new File(dataDir, ImmersiveEnchanting.MODID);
                            immersiveEnchantingDir.mkdirs();
                            File enchantmentCostsDir = new File(immersiveEnchantingDir, "enchantment_costs");
                            enchantmentCostsDir.mkdirs();

                            createEnchantmentJsonFiles(level.registryAccess(), enchantmentCostsDir);
                            createInternalJsonFiles(enchantmentCostsDir);
                            createPackMcMeta(rootDir);

                            Component message = Component.translatable("immersiveenchanting.command.generate_empty_datapack")
                                    .withStyle(style -> {
                                        return style
                                                .withColor(ChatFormatting.GREEN)
                                                .withUnderlined(true)
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.COPY_TO_CLIPBOARD, rootDir.getAbsolutePath()
                                                ));
                                    });

                            context.getSource().sendSuccess(() -> message, false);

                            return 1;
                        }
                )
        );

    }

    /**
     * Iterates every registered enchantment and writes an empty cost JSON file for each one,
     * one level slot per max level of that enchantment. Files are placed under
     * {@code enchantment_costs/<namespace>/<enchantment_name>.json}.
     */
    private static void createEnchantmentJsonFiles(RegistryAccess registryAccess, File enchantmentCostsDir) {
        List<Holder<Enchantment>> enchantments = EnchantmentUtil.getAllRegisteredEnchantments(registryAccess);
        for (Holder<Enchantment> enchantmentHolder : enchantments) {
            File jsonFile = getOrCreateFile(enchantmentHolder, enchantmentCostsDir);

            int maxLevel = enchantmentHolder.value().getMaxLevel();
            JsonObject jsonObject = buildEmptyCostJson(maxLevel);

            writeJsonFile(jsonFile, jsonObject);
        }
    }

    /**
     * Creates stub JSON files for the three mod-internal cost entries: {@code transmute},
     * {@code replicate}, and {@code enchanting_fuels}. Each file contains one empty cost slot
     * per level (1 level for transmute/replicate, 5 for fuels).
     */
    private static void createInternalJsonFiles(File enchantmentCostsDir) {
        File modNamespaceDir = new File(enchantmentCostsDir, ImmersiveEnchanting.MODID);
        modNamespaceDir.mkdirs();

        File transmuteFile = new File(modNamespaceDir, "transmute.json");
        JsonObject transmuteJson = buildEmptyCostJson(1);
        File replicateFile = new File(modNamespaceDir, "replicate.json");
        JsonObject replicateJson = buildEmptyCostJson(1);
        File enchantingFuelsFile = new File(modNamespaceDir, "enchanting_fuels.json");
        JsonObject enchantingFuelsJson = buildEmptyCostJson(5);
        writeJsonFile(transmuteFile, transmuteJson);
        writeJsonFile(replicateFile, replicateJson);
        writeJsonFile(enchantingFuelsFile, enchantingFuelsJson);
    }

    /**
     * Writes {@code pack.mcmeta} into the datapack root with a fixed pack format and description.
     */
    private static void createPackMcMeta(File rootDir) {
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "Datapack generated from /immersiveenchanting generateEmptyDatapack.");
        pack.addProperty("pack_format", PACK_FORMAT);
        pack.addProperty("min_format", PACK_FORMAT);
        pack.addProperty("max_format", PACK_FORMAT);

        JsonObject root = new JsonObject();
        root.add("pack", pack);
        File packmcmeta = new File(rootDir, "pack.mcmeta");

        writeJsonFile(packmcmeta, root);
    }

    /**
     * Resolves the output {@link File} path for an enchantment's JSON, splitting the registered
     * name on {@code :} to derive {@code <enchantmentCostsDir>/<namespace>/<name>.json}.
     * Creates the namespace subdirectory if it does not already exist.
     */
    private static @NotNull File getOrCreateFile(Holder<Enchantment> enchantmentHolder, File enchantmentCostsDir) {
        String id = enchantmentHolder.getRegisteredName();
        String[] parts = id.split(":", 2);

        String namespace = parts[0];
        String enchantName = parts[1];

        File namespaceDir = new File(enchantmentCostsDir, namespace);
        File enchantmentFile = new File(namespaceDir, enchantName + ".json");

        File parent = enchantmentFile.getParentFile();
        if (!parent.exists()) parent.mkdirs();

        return enchantmentFile;
    }

    /**
     * Builds a cost JSON object with {@code maxLevel} level entries, each containing a single
     * no-op cost (air × 0, 0 XP levels). The resulting structure matches the schema expected
     * by {@link me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack}.
     */
    private static JsonObject buildEmptyCostJson(int maxLevel) {
        JsonObject levels = new JsonObject();

        for (int i = 1; i <= maxLevel; i++) {
            JsonArray costArray = new JsonArray();

            JsonObject itemStack = new JsonObject();
            itemStack.addProperty("item_or_tag_id", "minecraft:air");
            itemStack.addProperty("count", 0);
            itemStack.addProperty("xp_levels", 0);

            JsonObject wrapper = new JsonObject();
            wrapper.add("item_stack", itemStack);

            costArray.add(wrapper);

            levels.add(String.valueOf(i), costArray);
        }

        JsonObject root = new JsonObject();
        root.addProperty("enabled", true);
        root.add("levels", levels);

        return root;
    }

    /**
     * Serializes {@code json} to {@code file} with pretty-printing, silently printing the stack trace on failure.
     */
    private static void writeJsonFile(File file, JsonObject json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


