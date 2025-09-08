package me.money.star.util.traits;

import com.google.common.eventbus.EventBus;
import net.minecraft.client.MinecraftClient;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface Util {
    MinecraftClient mc = MinecraftClient.getInstance();
    EventBus EVENT_BUS = new EventBus();
    Random RANDOM = ThreadLocalRandom.current();
}
