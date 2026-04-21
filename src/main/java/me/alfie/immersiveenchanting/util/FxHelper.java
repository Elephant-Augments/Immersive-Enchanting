package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSound;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FxHelper {

    private static float lastRemoveSoundStep;

    public static void playToolSlotChanged(Level level) {
        playClientUISound(level, SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.5f, 1f);
        playClientUISound(level, SoundEvents.BOOK_PAGE_TURN, 0.3f, 1.2f);
    }

    public static void playNodeHover(Level level, Node node) {
        if(node.isState(NodeState.LOCKED) || node.isState(NodeState.ALERT)) {
            playGenericNodeHover(level);
            return;
        }

        if(doesSoundExist(node.id())) {
            NodeSound nodeSound = NodeSoundsDatapack.map.get(node.id());
            int nodeLevel = node.getEnchantmentLevel();
            float defaultPitch = nodeSound.pitch();
            float newPitch = defaultPitch + (nodeLevel - 1) * 0.5f;
            newPitch = Math.min(newPitch, 2.0f);

            playClientUISound(level, node.id(), nodeSound.volume(), newPitch);
        } else {
            playGenericNodeHover(level);
        }

        if(node.getEnchantmentLevel() == CostRegistry.client().get(node.id()).levelCosts().maxLevel()) {
            playClientUISound(level, SoundEvents.AMETHYST_BLOCK_RESONATE, 1f, 2);
        }
    }

    private static void playGenericNodeHover(Level level) {
        playClientUISound(level, SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED, 0.5f, 1f);
    }

    public static void playTooltipLock(Level level) {
        playClientUISound(level, SoundEvents.DISPENSER_FAIL, 0.5f, 2f);
    }

    public static void playEnchantSuccess(Level level, BlockPos tablePos, boolean isHighestTier) {
        if(isHighestTier) {
            level.playSound(null, tablePos, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, tablePos, SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
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

    public static void playTransmute(ServerLevel level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ENDER_CHEST_OPEN,
                SoundSource.MASTER, 0.4F, 1.0F);
        level.playSound(null, tablePos, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.MASTER, 0.8F, 0.8F);
        level.playSound(null, tablePos, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.MASTER, 0.8F, 1F);
        level.playSound(null, tablePos, SoundEvents.EVOKER_PREPARE_SUMMON,
                SoundSource.MASTER, 0.1F, 1.2F);

        int particleCount = 70;
        level.sendParticles(
                ParticleTypes.ENCHANT,
                tablePos.getX() + 0.5,
                tablePos.getY() + 1,
                tablePos.getZ() + 0.5,
                particleCount,
                0.2, 0.2, 0.2,
                0.1);

        level.sendParticles(
                ParticleTypes.GLOW,
                tablePos.getX() + 0.5,
                tablePos.getY() + 1,
                tablePos.getZ() + 0.5,
                particleCount,
                0.2, 0.2, 0.2,
                0.1);
    }

    public static void playReplicate(ServerLevel level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ALLAY_ITEM_GIVEN,
                SoundSource.MASTER, 0.7F, 1.2F);
        level.playSound(null, tablePos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
                SoundSource.MASTER, 0.8F, 1.5F);
        level.playSound(null, tablePos, SoundEvents.BOOK_PAGE_TURN,
                SoundSource.MASTER, 0.5F, 1.5F);
        level.playSound(null, tablePos, SoundEvents.ILLUSIONER_MIRROR_MOVE,
                SoundSource.MASTER, 0.4F, 1.2F);

        int particleCount = 30;
        level.sendParticles(
                ParticleTypes.END_ROD,
                tablePos.getX() + 0.5,
                tablePos.getY() + 1,
                tablePos.getZ() + 0.5,
                particleCount,
                0.1, 0.1, 0.1,
                0.1);
    }

    public static void playRemoveProgress(Level level, float progress) {
        float step = (float) Math.floor(progress * 10f);

        if (step == lastRemoveSoundStep) return;
        lastRemoveSoundStep = step;

        float pitch = 1.8f - progress;

        playClientUISound(level, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, pitch);
    }

    public static void playEnchantmentRemove(Level level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.MASTER, 0.5F, 1.2F);
    }
}
