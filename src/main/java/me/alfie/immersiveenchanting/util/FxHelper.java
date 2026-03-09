package me.alfie.immersiveenchanting.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class FxHelper {

    public static void playBiblioclasmSpawnFx(Level level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.SOUL_SAND_BREAK,
                SoundSource.BLOCKS, 0.4F, 1.2F);
        level.playSound(null, blockPos, SoundEvents.SNOW_BREAK,
                SoundSource.BLOCKS, 0.4F, 1.2F);
        level.playSound(null, blockPos, SoundEvents.GENERIC_BURN,
                SoundSource.BLOCKS, 0.1F, 0.5F);
    }
}
