package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import me.money.star.mixin.MixinMinecraftClient;

/**
 * Allows mining and eating at the same time
 *
 * @see MixinMinecraftClient
 */
@Cancelable
public class ItemMultitaskEvent extends Event {

}
