package me.money.star.client.modules.client;

import me.money.star.client.gui.modules.Module;


public class Notifications extends Module {
    public static Notifications INSTANCE = new Notifications();

    public Notifications() {
        super("Notifications", "Notify on enable module", Category.CLIENT, true, false, false);
        INSTANCE = this;
    }

}