package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.manager.RenderManager;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.PlayerListColumnsEvent;
import me.money.star.event.impl.PlayerListIconEvent;
import me.money.star.event.impl.gui.hud.PlayerListEvent;
import me.money.star.event.impl.gui.hud.PlayerListNameEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;


public class ExtraTab extends Module {

    public Setting<Boolean> self = bool("Self", false);
    public Setting<Boolean> friends = bool("Friends", false);


    public ExtraTab() {
        super("ExtraTab", "Expands the tab list size to allow for more players",
                Category.MISC,true,false,false);
    }
    @Subscribe
    public void onPlayerListName(PlayerListNameEvent event)
    {
        String[] names = event.getPlayerName().getString().split(" ");
        if (self.getValue())
        {
            for (String s : names)
            {
                String name1 = stripControlCodes(s);
                if (name1.equals(mc.getGameProfile().getName()))
                {
                    event.cancel();
                    event.setPlayerName(Text.of((Formatting.DARK_PURPLE + event.getPlayerName().getString())));
                    return;
                }
            }
        }
        if (friends.getValue())
        {
            for (String s : names)
            {
                String name1 = stripControlCodes(s);
                if (MoneyStar.friendManager.isFriend(name1))
                {
                    event.cancel();
                    event.setPlayerName(Text.of(Formatting.AQUA + event.getPlayerName().getString()));
                    break;
                }
            }
        }
    }

    private String stripControlCodes(String string)
    {
        StringBuilder builder = new StringBuilder();
        boolean skip = false;
        for (char c : string.toCharArray())
        {
            if (c == Formatting.FORMATTING_CODE_PREFIX)
            {
                skip = true;
                continue;
            }
            if (skip)
            {
                skip = false;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }
}


