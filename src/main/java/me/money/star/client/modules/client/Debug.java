package me.money.star.client.modules.client;

import me.money.star.client.gui.modules.ConcurrentModule;
import me.money.star.client.settings.Setting;


public class Debug extends ConcurrentModule {
    public static Debug INSTANCE = new Debug();
    public Setting<Boolean> desc = bool("Descriptions", false);
    public Setting<Boolean> outline = bool("Outline", false);
    public Setting<Integer> line = num("LineWidth", 1,0,5);

    public Setting<Boolean> colorRect = bool("ColorRect", false);
    public Setting<Boolean> gear = bool("Gear", false);
    public Setting<String> plus = str("Plus", "+");
    public Setting<String> minus = str("Minus", "-");

    public Debug() {
        super("Debug", "More settings", Category.CLIENT, true, false, false);
        INSTANCE = this;
    }

}