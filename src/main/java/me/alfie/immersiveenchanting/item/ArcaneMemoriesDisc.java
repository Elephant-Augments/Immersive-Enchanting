package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.sound.ModSounds;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;

public class ArcaneMemoriesDisc extends RecordItem {

    public ArcaneMemoriesDisc() {
        super(15, ModSounds.ARCANE_MEMORIES,
                new Properties().stacksTo(1).rarity(Rarity.RARE), 156*20);
    }
}
