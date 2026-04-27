package me.alfie.immersiveenchanting.networking;


import net.minecraftforge.network.NetworkEvent;

public interface ModNetworkPacket {

    void exec(NetworkEvent.Context context);
}
