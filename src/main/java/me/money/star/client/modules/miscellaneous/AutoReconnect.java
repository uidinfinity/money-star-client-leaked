package me.money.star.client.modules.miscellaneous;

import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;


public class AutoReconnect extends Module {

    public Setting<Integer> delay = num("Delay", 5, 0, 20);

    public AutoReconnect() {
        super("AutoReconnect", "You are automatically reconnected to the server after the crash.", Category.MISC,true,false,false);
    }


}
