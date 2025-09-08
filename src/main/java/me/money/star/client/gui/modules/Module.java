package me.money.star.client.gui.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.money.star.MoneyStar;
import me.money.star.client.modules.client.Notifications;
import me.money.star.event.impl.ClientEvent;
import me.money.star.event.impl.Render2DEvent;
import me.money.star.event.impl.Render3DEvent;
import me.money.star.client.System;
import me.money.star.client.commands.Command;
import me.money.star.client.settings.Bind;
import me.money.star.client.settings.Setting;
import me.money.star.client.manager.client.ConfigManager;
import me.money.star.util.traits.Jsonable;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Formatting;

public class Module extends System implements Jsonable {
    public final String description;
    public final Category category;
    public Setting<Boolean> enabled = bool("Enabled", false);
    public Setting<Boolean> drawn = bool("Drawn", true);
    public Setting<Bind> bind = key("Bind:", new Bind(-1));

    public Setting<String> displayName;
    public boolean hasListener;
    public boolean alwaysListening;
    public boolean hidden;



    public Module(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening
    ) {
        super(name);
        this.displayName = str("DisplayName", name);
        this.description = description;
        this.category = category;
        this.hasListener = hasListener;
        this.hidden = hidden;
        this.alwaysListening = alwaysListening;

    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onToggle() {
    }

    public void onLoad() {
    }

    public void onTick() {
    }

    public void onUpdate() {
    }

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public void onUnload() {
    }

    public String getDisplayInfo() {
        return null;
    }

    public boolean isOn() {
        return this.enabled.getValue();
    }

    public boolean isOff() {
        return !this.enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void enable() {
        this.enabled.setValue(true);
        this.onToggle();
        this.onEnable();
        if (this.isOn() && this.hasListener && !this.alwaysListening) {
            Util.EVENT_BUS.register(this);
        }
        if (Notifications.INSTANCE.isEnabled()) {
            Command.sendMessage(getName() +"."+Formatting.GREEN+ "Enable");
        }
    }

    public void disable() {
        if (this.hasListener && !this.alwaysListening) {
            Util.EVENT_BUS.unregister(this);
        }
        this.enabled.setValue(false);
        this.onToggle();
        this.onDisable();
        if (Notifications.INSTANCE.isEnabled()) {
            Command.sendMessage(getName() +"."+Formatting.RED+ "Disable");
        }
    }

    public void toggle() {
        ClientEvent event = new ClientEvent(!this.isEnabled() ? 1 : 0, this);
        Util.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            this.setEnabled(!this.isEnabled());
        }
    }

    public String getDisplayName() {
        return this.displayName.getValue();
    }

    public void setDisplayName(String name) {
        Module module = MoneyStar.moduleManager.getModuleByDisplayName(name);
        Module originalModule = MoneyStar.moduleManager.getModuleByName(name);
        if (module == null && originalModule == null) {
            Command.sendMessage(this.getDisplayName() + ", name: " + this.getName() + ", has been renamed to: " + name);
            this.displayName.setValue(name);
            return;
        }
        Command.sendMessage(Formatting.RED + "A module of this name already exists.");
    }

    @Override public boolean isEnabled() {
        return isOn();
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public void setDrawn(boolean drawn) {
        this.drawn.setValue(drawn);
    }

    public Category getCategory() {
        return this.category;
    }

    public String getInfo() {
        return null;
    }

    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }

    public boolean listening() {
        return this.hasListener && this.isOn() || this.alwaysListening;
    }
    public void sendPacket(Packet<?> packet){
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }
    public String getFullArrayString() {
        return this.getDisplayName() + Formatting.GRAY + (this.getDisplayInfo() != null ? " [" + Formatting.WHITE + this.getDisplayInfo() + Formatting.GRAY + "]" : "");
    }

    @Override public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Setting<?> setting : getSettings()) {
            try {
                if (setting.getValue() instanceof Bind bind) {
                    object.addProperty(setting.getName(), bind.getKey());
                } else {
                    object.addProperty(setting.getName(), setting.getValueAsString());
                }
            } catch (Throwable e) {
            }
        }
        return object;
    }

    @Override public void fromJson(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        String enabled = object.get("Enabled").getAsString();
        if (Boolean.parseBoolean(enabled)) toggle();
        for (Setting<?> setting : getSettings()) {
            try {
                ConfigManager.setValueFromJson(this, setting, object.get(setting.getName()));
            } catch (Throwable throwable) {
            }
        }
    }

    public enum Category {
        COMBAT("Combat"),
        MISC("Miscellaneous"),
        RENDER("Render"),
        MOVEMENT("Movement"),
        WORLD("World"),
        EXPLOIT("Exploit"),

        CLIENT("Client");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

}
