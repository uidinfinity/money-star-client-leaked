package me.money.star.event.impl;

import me.money.star.event.Event;
import me.money.star.mixin.MixinMinecraftClient;

/**
 * The main game loop event, this "tick" runs while the
 * {@link net.minecraft.client.MinecraftClient#running} var is <tt>true</tt>.
 *
 * @author linus
 * @see MixinMinecraftClient
 * @since 1.0
 */
public class RunTickEvent extends Event {

}
