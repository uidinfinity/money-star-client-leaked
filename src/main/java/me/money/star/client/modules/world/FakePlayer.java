package me.money.star.client.modules.world;


import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.entity.player.PushEntityEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.util.traits.Util;
import me.money.star.util.world.FakePlayerEntity;

public class FakePlayer extends Module {
    //
    private FakePlayerEntity fakePlayer;

    public Setting<String> name = str("Name", "Aleksander Nevskiy");
    public FakePlayer() {
        super("FakePlayer", "Spawns an indestructible client-side player",
                Category.WORLD,true,false,false);
    }

    @Override
    public void onEnable() {
        if (Util.mc.player != null && Util.mc.world != null) {
            fakePlayer = new FakePlayerEntity(Util.mc.player, name.getValue());
            fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null) {
            fakePlayer.despawnPlayer();
            fakePlayer = null;
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        fakePlayer = null;
        disable();
    }

    @Subscribe
    public void onPushEntity(PushEntityEvent event)
    {
        // Prevents Simulation flags (as the FakePlayer is client only, so Grim rightfully
        // flags us for that push motion that shouldn't happen
        if (event.getPushed().equals(Util.mc.player) && event.getPusher().equals(fakePlayer))
        {
            event.setCancelled(true);
        }
    }
}
