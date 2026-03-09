package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.enchanting.EnchantingNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FxHelper {

    //Client (Player)
    public static void playGenericUISound(Player player) {
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.3f, 1f);
    }

    public static void playEnchantingNodeHoverSound(EnchantingNode enchantingNode, Player player) {
        float pitch = 1;
        int highestLevel = EnchantmentCostRegistry.getClientRegistry()
                .getEnchantmentCost(enchantingNode.getEnchantment())
                .getHighestLevel();

        int nodeEnchantmentLevel = enchantingNode.getEnchantmentLevel();

        if(nodeEnchantmentLevel == highestLevel) {
            pitch = 2;
            player.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1f, 2);
        }
        player.playSound(SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED, 1f, pitch);
    }

    public static void playEnchantingTableToolSlotSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.5f, 1f);
    }

    public static void playTooltipLockSound(Player player) {
        player.playSound(SoundEvents.DISPENSER_FAIL, 1f, 2f);
    }

    public static void playRemoveProgressSound(Player player, int lastBars, int bars, float progress) {
        for (int i = lastBars; i < bars; i++) {
            float pitch = Math.max(1f, 2f - progress);
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.3f, pitch);
        }
    }

    //Server (Level)
    public static void playEnchantSuccessFx(Level level, BlockPos tablePos, boolean isHighestTier) {
        if(isHighestTier) {
            level.playSound(null, tablePos, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, tablePos, SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static void playEnchantmentRemoveSound(Level level, Player player) {
        level.playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.MASTER, 0.5F, 1.2F);
    }

    public static void playEnchantFailFx(Level level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.MASTER, 1.0F, 1.0F);
    }

    public static void playReplicateFx(Level level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ALLAY_ITEM_GIVEN,
                SoundSource.MASTER, 0.7F, 1.2F);
        level.playSound(null, tablePos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
                SoundSource.MASTER, 0.8F, 1.5F);
        level.playSound(null, tablePos, SoundEvents.BOOK_PAGE_TURN,
                SoundSource.MASTER, 0.5F, 1.5F);
        level.playSound(null, tablePos, SoundEvents.ILLUSIONER_MIRROR_MOVE,
                SoundSource.MASTER, 0.4F, 1.2F);

        int particleCount = 30;
        ((ServerLevel) level).sendParticles(
                ParticleTypes.END_ROD,
                tablePos.getX() + 0.5,
                tablePos.getY() + 1,
                tablePos.getZ() + 0.5,
                particleCount,
                0.1, 0.1, 0.1,
                0.1);
    }

    public static void playTransmuteFx(Level level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ENDER_CHEST_OPEN,
                SoundSource.MASTER, 0.4F, 1.0F);
        level.playSound(null, tablePos, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.MASTER, 0.8F, 0.8F);
        level.playSound(null, tablePos, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.MASTER, 0.8F, 1F);
        level.playSound(null, tablePos, SoundEvents.EVOKER_PREPARE_SUMMON,
                SoundSource.MASTER, 0.1F, 1.2F);

        int particleCount = 70;
        ((ServerLevel) level).sendParticles(
                ParticleTypes.ENCHANT,
                tablePos.getX() + 0.5,
                tablePos.getY() + 1,
                tablePos.getZ() + 0.5,
                particleCount,
                0.2, 0.2, 0.2,
                0.1);

        ((ServerLevel) level).sendParticles(
                ParticleTypes.GLOW,
                tablePos.getX() + 0.5,
                tablePos.getY() + 1,
                tablePos.getZ() + 0.5,
                particleCount,
                0.2, 0.2, 0.2,
                0.1);
    }

    public static void playBiblioclasmSpawnFx(Level level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.BLOCKS, 1F, 0.8F);
        level.playSound(null, blockPos, SoundEvents.SNOW_BREAK,
                SoundSource.BLOCKS, 0.4F, 1.2F);
        level.playSound(null, blockPos, SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED,
                SoundSource.BLOCKS, 0.8F, 0.4F);
        level.playSound(null, blockPos, SoundEvents.GENERIC_BURN,
                SoundSource.BLOCKS, 0.1F, 0.5F);

        int particleCount = 30;
        ((ServerLevel) level).sendParticles(
                ParticleTypes.SOUL,
                blockPos.getX() + 0.5,
                blockPos.getY() + 1,
                blockPos.getZ() + 0.5,
                particleCount,
                0.2, 0.2, 0.2,
                0.1);
    }
}
