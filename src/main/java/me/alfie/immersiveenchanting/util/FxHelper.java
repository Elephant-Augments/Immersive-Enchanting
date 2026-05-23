package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.api.node.internal.ModFilterNodeData;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSound;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Handles client and server-side sound + particle effects for the enchanting UI.
 * <p>
 * Centralizes all feedback (UI sounds, node interactions, enchanting events, etc.)
 * so behavior stays consistent and easy to tweak.
 * <p>
 * Includes:
 * - UI interaction sounds (hover, clicks, errors)
 * - Node-specific sounds (datapack-driven)
 * - Enchanting event effects (success, transmute, replicate, remove)
 * - Lightweight progression feedback (e.g. removal progress ticks)
 * <p>
 * Most methods are fire-and-forget helpers and assume valid inputs.
 */
public class FxHelper {

    private static float lastRemoveSoundStep;

    /**
     * Plays the sounds for when an item is placed into or removed from the tool slot.
     */
    public static void playToolSlotChanged(Player player) {
        playClientUISound(player, SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.5f, 1f);
        playClientUISound(player, SoundEvents.BOOK_PAGE_TURN, 0.3f, 1.2f);
    }

    private static void playClientUISound(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playLocalSound(player, sound, SoundSource.MASTER, volume, pitch);
    }

    /**
     * Plays the hover sound for a node. Uses the datapack-defined sound for the node's enchantment
     * if one is registered (pitch-shifted by level), otherwise falls back to a generic pickup sound.
     * Plays an extra resonate chime if the node is at its maximum level.
     * No-ops if node hover sounds are disabled in config.
     */
    public static void playNodeHover(Player player, Node node) {
        if (!ClientConfig.areNodeHoverSoundsEnabled()) return;

        if (node.isDataType(ModFilterNodeData.TYPE)) {
            playModFilterNodeHover(player);
            return;
        }

        if (node.isState(NodeState.LOCKED) || node.isState(NodeState.ALERT)) {
            playGenericNodeHover(player);
            return;
        }

        if (doesSoundExist(node.branchId())) {
            NodeSound nodeSound = NodeSoundMap.client().get(node.branchId());
            int nodePosition = node.getPosition();
            float defaultPitch = nodeSound.pitch();
            float newPitch = defaultPitch + (nodePosition) * 0.5f;
            newPitch = Math.min(newPitch, 2.0f);

            playClientUISound(player, node.branchId(), nodeSound.volume(), newPitch);
        } else {
            playGenericNodeHover(player);
        }

        if (node.getTier().equals(NodeTier.ELITE)) {
            playClientUISound(player, SoundEvents.AMETHYST_BLOCK_RESONATE, 1f, 2);
        }
    }

    private static void playModFilterNodeHover(Player player) {
        float randomPitch = player.getRandom().nextFloat() * 2;
        playClientUISound(player, SoundEvents.DISPENSER_DISPENSE, 0.5f, randomPitch);
    }

    private static void playGenericNodeHover(Player player) {
        playClientUISound(player, SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED, 0.5f, 1f);
    }

    public static boolean doesSoundExist(ResourceLocation id) {
        if (NodeSoundMap.client().containsKey(id)) {
            NodeSound nodeSound = NodeSoundMap.client().get(id);
            return BuiltInRegistries.SOUND_EVENT.containsKey(nodeSound.sound());
        }

        return false;
    }

    private static void playClientUISound(Player player, ResourceLocation id, float volume, float pitch) {
        if(doesSoundExist(id)) {
            SoundEvent sound = getSoundEvent(id);
            playClientUISound(player, sound, volume, pitch);
        }
    }

    public static SoundEvent getSoundEvent(ResourceLocation id) {
        if(doesSoundExist(id)) {
            return BuiltInRegistries.SOUND_EVENT.get(NodeSoundMap.client().get(id)
                    .sound());
        }
        throw new IllegalArgumentException(id + " is not a valid sound identifier!");
    }

    public static void playTooltipLock(Player player) {
        playClientUISound(player, SoundEvents.DISPENSER_FAIL, 0.5f, 2f);
    }

    public static void playEnchantSuccess(Level level, BlockPos tablePos, boolean isHighestTier) {
        if (isHighestTier) {
            level.playSound(null, tablePos, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, tablePos, SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
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

    /**
     * Plays a tick sound tied to the hold-to-remove progress. Fires once per 10% increment
     * and lowers in pitch as progress increases, giving audio feedback during removal.
     */
    public static void playRemoveProgress(Player player, float progress) {
        float step = (float) Math.floor(progress * 10f);

        if (step == lastRemoveSoundStep) return;
        lastRemoveSoundStep = step;

        float pitch = 1.8f - progress;

        playClientUISound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, pitch);
    }

    public static void playEnchantmentRemove(Level level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.MASTER, 0.5F, 1.2F);
    }

    public static void playGenericUISound(Player player) {
        playClientUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.3f, 1f);
    }

    public static void playTabDown(Player player) {
        playClientUISound(player, SoundEvents.DISPENSER_DISPENSE, 0.5f, 0.8f);
    }

    public static void playTabUp(Player player) {
        playClientUISound(player, SoundEvents.DISPENSER_DISPENSE, 0.5f, 1.2f);
    }
}
