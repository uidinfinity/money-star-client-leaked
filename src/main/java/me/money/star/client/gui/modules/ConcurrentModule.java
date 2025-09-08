package me.money.star.client.gui.modules;

import me.money.star.util.traits.Util;

public class ConcurrentModule extends Module {
    public ConcurrentModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening)
    {
        super(name, description, category,hasListener,hidden,alwaysListening);
        Util.EVENT_BUS.register(this);
    }
}
