package me.alfie.immersiveenchanting.sound;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ImmersiveEnchanting.MODID);

    public static final Supplier<SoundEvent> BIBLIOCLASM = SOUND_EVENTS.register("biblioclasm",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "biblioclasm")));

    public static final Supplier<SoundEvent> ARCANE_MEMORIES = SOUND_EVENTS.register("arcane_memories",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "arcane_memories")));

    public static final ResourceKey<JukeboxSong> BIBLIOCLASM_KEY = ResourceKey.create(Registries.JUKEBOX_SONG,
            ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "biblioclasm"));

    public static final ResourceKey<JukeboxSong> ARCANE_MEMORIES_KEY = ResourceKey.create(Registries.JUKEBOX_SONG,
            ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "arcane_memories"));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
