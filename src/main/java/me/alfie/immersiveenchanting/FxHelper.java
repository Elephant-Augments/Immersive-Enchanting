package me.alfie.immersiveenchanting;

import ca.weblite.objc.Client;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSound;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundInstances;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class FxHelper {

    public static void playToolSlotChanged(Level level) {
        playClientUISound(level, SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.5f, 1f);
        playClientUISound(level, SoundEvents.BOOK_PAGE_TURN, 0.3f, 1.2f);
    }

    public static void playNodeHover(Level level, Node node) {
        if(doesSoundExist(node.id())) {
            NodeSound nodeSound = NodeSoundsDatapack.map.get(node.id());

            int nodeLevel = node.getEnchantmentLevel();
            float defaultPitch = nodeSound.pitch();
            float newPitch = defaultPitch + (nodeLevel - 1) * 0.5f;
            newPitch = Math.min(newPitch, 2.0f);

            playClientUISound(level, node.id(), nodeSound.volume(), newPitch);
        }

        playClientUISound(level, SoundEvents.CHISELED_BOOKSHELF_BREAK, 0.2f, 1f);
        if(node.getEnchantmentLevel() == EnchantmentCostRegistry.get(node.id()).levelCosts().maxLevel()) {
            playClientUISound(level, SoundEvents.AMETHYST_BLOCK_RESONATE, 1f, 2);
        }
    }

    public static void playTooltipLock(Level level) {
        playClientUISound(level, SoundEvents.DISPENSER_FAIL, 0.5f, 2f);
    }

    public static void playEnchant(Player player) {
        player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1f, 1f);
    }

    private static void playClientUISound(Level level, SoundEvent sound, float volume, float pitch) {
        level.playPlayerSound(sound, SoundSource.UI, volume, pitch);
    }

    private static void playClientUISound(Level level, Identifier id, float volume, float pitch) {
        if(doesSoundExist(id)) {
            SoundEvent sound = getSoundEvent(id);
            playClientUISound(level, sound, volume, pitch);
        }
    }

    public static boolean doesSoundExist(Identifier id) {
        if (NodeSoundsDatapack.map.containsKey(id)) {
            NodeSound nodeSound = NodeSoundsDatapack.map.get(id);
            Optional<Holder.Reference<SoundEvent>> soundEvent = BuiltInRegistries.SOUND_EVENT.get(nodeSound.sound());
            return soundEvent.isPresent();
        }

        return false;
    }

    public static SoundEvent getSoundEvent(Identifier id) {
        if(doesSoundExist(id)) {
            return BuiltInRegistries.SOUND_EVENT.get(NodeSoundsDatapack.map.get(id).sound()).get().value();
        }
        throw new IllegalArgumentException(id + " is not a valid sound identifier!");
    }

}
