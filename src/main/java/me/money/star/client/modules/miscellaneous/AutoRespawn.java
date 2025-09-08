package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.commands.Command;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.ScreenOpenEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.Formatting;


public class AutoRespawn extends Module {
    //
    private boolean respawn;
    public Setting<Boolean> deathCoords = bool("DeathPosition", false);
    public AutoRespawn() {
        super("AutoRespawn", "Respawns automatically after a death",
                Category.MISC,true,false,false);
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE && respawn && Util.mc.player.isDead()) {
            Util.mc.player.requestRespawn();
            respawn = false;
            if (deathCoords.getValue())
               Command.sendMessage(Formatting.WHITE + "You died at " + "X:" + (int) Util.mc.player.getX() + " " + "Y:" + (int) Util.mc.player.getY() + " " + "Z:" + (int) Util.mc.player.getZ());
        }

    }

    @Subscribe
    public void onScreenOpen(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen) {
            respawn = true;
        }
    }
}
