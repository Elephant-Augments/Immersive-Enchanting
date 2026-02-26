package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.sound.ModSounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class BiblioclasmDisc extends Item {

    public BiblioclasmDisc(Properties properties) {
        super(properties.jukeboxPlayable(ModSounds.BIBLIOCLASM_KEY).stacksTo(1).rarity(Rarity.RARE));
    }
}
