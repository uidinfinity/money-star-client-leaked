package me.money.star.event.impl;

import me.money.star.client.System;
import me.money.star.client.settings.Setting;
import me.money.star.event.Event;

public class ClientEvent extends Event {
    private System feature;
    private Setting<?> setting;
    private int stage;

    public ClientEvent(int stage, System feature) {
        this.stage = stage;
        this.feature = feature;
    }

    public ClientEvent(Setting<?> setting) {
        this.stage = 2;
        this.setting = setting;
    }

    public System getFeature() {
        return this.feature;
    }

    public Setting<?> getSetting() {
        return this.setting;
    }

    public int getStage() {
        return stage;
    }
}
