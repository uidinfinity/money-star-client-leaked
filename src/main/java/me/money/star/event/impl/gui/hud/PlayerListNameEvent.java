package me.money.star.event.impl.gui.hud;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.text.Text;

import java.util.UUID;

@Cancelable
public class PlayerListNameEvent extends Event {
    private Text playerName;
    private final UUID id;

    public PlayerListNameEvent(Text playerName, UUID id)
    {
        this.playerName = playerName;
        this.id = id;
    }

    public void setPlayerName(Text playerName)
    {
        this.playerName = playerName;
    }

    public Text getPlayerName()
    {
        return playerName;
    }

    public UUID getId()
    {
        return id;
    }
}