package me.alfie.immersiveenchanting.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class CreativeBookshelfBlock extends Block {
    public CreativeBookshelfBlock(Properties properties) {
        super(properties
                .destroyTime(2.0f)
                .explosionResistance(10.0f)
                .sound(SoundType.STONE));
    }
}
