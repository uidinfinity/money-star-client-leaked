package me.money.star.util.discord.callbacks;

import com.sun.jna.Callback;
import me.money.star.util.discord.DiscordUser;


public interface JoinRequestCallback extends Callback {
    void apply(final DiscordUser p0);
}
