package me.money.star.event.impl;

import me.money.star.client.settings.Setting;
import me.money.star.event.StageEvent;


public class SettingsUpdateEvent extends StageEvent {
    //
    private final Setting<?> config;

    /**
     * @param config
     */
    public SettingsUpdateEvent(Setting<?> config) {
        this.config = config;
    }

    /**
     * @return
     */
    public Setting<?> getConfig() {
        return config;
    }
}
