package me.alfie.immersiveenchanting.api.datapack.manager;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class ClientDatapackUpdatedEvent extends Event {

    private final Player player;

    public ClientDatapackUpdatedEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
