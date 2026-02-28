package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.sound.ModSounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ArcaneMemoriesDisc extends Item {

    public ArcaneMemoriesDisc(Properties properties) {
        super(properties.jukeboxPlayable(ModSounds.ARCANE_MEMORIES_KEY).stacksTo(1).rarity(Rarity.RARE));
    }
}
