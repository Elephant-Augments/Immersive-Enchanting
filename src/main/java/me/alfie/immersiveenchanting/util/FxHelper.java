package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSound;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FxHelper {

    private static float lastRemoveSoundStep;

    public static void playToolSlotChanged(Player player) {
        playClientUISound(player, SoundEvents.ARMOR_EQUIP_GENERIC, 0.5f, 1f);
        playClientUISound(player, SoundEvents.BOOK_PAGE_TURN, 0.3f, 1.2f);
    }

    public static void playNodeHover(Player player, Node node) {
        if(!ClientConfig.areNodeHoverSoundsEnabled()) return;

        if(node.isState(NodeState.LOCKED) || node.isState(NodeState.ALERT)) {
            playGenericNodeHover(player);
            return;
        }

        if(doesSoundExist(node.id())) {
            NodeSound nodeSound = ClientDatapackManager.nodeSoundMap().get(node.id());
            int nodeLevel = node.getEnchantmentLevel();
            float defaultPitch = nodeSound.pitch();
            float newPitch = defaultPitch + (nodeLevel - 1) * 0.5f;
            newPitch = Math.min(newPitch, 2.0f);

            playClientUISound(player, node.id(), nodeSound.volume(), newPitch);
        } else {
            playGenericNodeHover(player);
        }

        if(node.getEnchantmentLevel() == CostRegistry.client().get(node.id()).levelCosts().maxLevel()) {
            playClientUISound(player, SoundEvents.AMETHYST_BLOCK_RESONATE, 1f, 2);
        }
    }

    private static void playGenericNodeHover(Player player) {
        playClientUISound(player, SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED, 0.5f, 1f);
    }

    public static void playTooltipLock(Player player) {
        playClientUISound(player, SoundEvents.DISPENSER_FAIL, 0.5f, 2f);
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

    private static void playClientUISound(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playLocalSound(player.blockPosition(), sound, SoundSource.MASTER, volume, pitch, false);
    }

    private static void playClientUISound(Player player, ResourceLocation id, float volume, float pitch) {
        if(doesSoundExist(id)) {
            SoundEvent sound = getSoundEvent(id);
            playClientUISound(player, sound, volume, pitch);
        }
    }

    public static boolean doesSoundExist(ResourceLocation id) {
        if (ClientDatapackManager.nodeSoundMap().containsKey(id)) {
            NodeSound nodeSound = ClientDatapackManager.nodeSoundMap().get(id);
            return BuiltInRegistries.SOUND_EVENT.containsKey(nodeSound.sound());
        }

        return false;
    }

    public static SoundEvent getSoundEvent(ResourceLocation id) {
        if(doesSoundExist(id)) {
            return BuiltInRegistries.SOUND_EVENT.get(ClientDatapackManager.nodeSoundMap().get(id)
                    .sound());
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
}
