package me.alfie.immersiveenchanting.api.datapack.manager;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

public class ServerDatapackUpdatedEvent extends Event {

    private final MinecraftServer server;

    public ServerDatapackUpdatedEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}

