package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.sound.ModSounds;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;

public class BiblioclasmDisc extends RecordItem {

    public BiblioclasmDisc() {
        super(15, ModSounds.BIBLIOCLASM,
                new Properties().stacksTo(1).rarity(Rarity.RARE), 127*20);
    }
}
