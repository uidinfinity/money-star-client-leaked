package me.money.star.event.impl;

import me.money.star.event.Event;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;


public class ConnectScreenEvent extends Event
{

    private final ServerAddress address;
    private final ServerInfo info;

    public ConnectScreenEvent(ServerAddress address, ServerInfo info)
    {
        this.address = address;
        this.info = info;
    }

    public ServerAddress getAddress()
    {
        return address;
    }

    public ServerInfo getInfo()
    {
        return info;
    }
}
