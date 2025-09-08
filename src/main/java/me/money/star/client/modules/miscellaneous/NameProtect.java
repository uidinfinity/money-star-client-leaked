package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.text.TextVisitEvent;
import me.money.star.util.traits.Util;

public class NameProtect extends Module {

    public Setting<String> name = str("Name", "Astolfo");
    public NameProtect() {
        super("NameProtect", "Refactor the player name in chat and tablist",
                Category.MISC,true,false,false);
    }

    @Subscribe
    public void onTextVisit(TextVisitEvent event) {
        if (Util.mc.player == null) {
            return;
        }
        final String username = Util.mc.getSession().getUsername();
        final String text = event.getText();
        if (text.contains(username)) {
            event.cancel();
            event.setText(text.replace(username, name.getValue()));
        }
    }
}
