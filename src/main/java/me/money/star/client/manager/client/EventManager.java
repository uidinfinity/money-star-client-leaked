package me.money.star.client.manager.client;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.event.Stage;
import me.money.star.event.impl.*;
import me.money.star.client.System;
import me.money.star.client.commands.Command;
import me.money.star.util.models.Timer;
import me.money.star.util.traits.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Formatting;

public class EventManager extends System {
    private final Timer logoutTimer = new Timer();

    public void init() {
        Util.EVENT_BUS.register(this);
    }

    public void onUnload() {
        Util.EVENT_BUS.unregister(this);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        Util.mc.getWindow().setTitle("Money-star v1.0-beta");
        if (!fullNullCheck()) {
            MoneyStar.moduleManager.onUpdate();
            MoneyStar.moduleManager.sortModules(true);
            onTick();
        }
    }

    public void onTick() {
        if (fullNullCheck())
            return;
        MoneyStar.moduleManager.onTick();
        for (PlayerEntity player : Util.mc.world.getPlayers()) {
            if (player == null || player.getHealth() > 0.0F)
                continue;
            Util.EVENT_BUS.post(new DeathEvent(player));
//            PopCounter.getInstance().onDeath(player);
        }
    }

    @Subscribe
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (fullNullCheck())
            return;
        if (event.getStage() == Stage.PRE) {
            MoneyStar.speedManager.updateValues();
            MoneyStar.positionManager.updatePosition();
        }
        if (event.getStage() == Stage.POST) {
            MoneyStar.positionManager.restorePosition();
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        MoneyStar.serverManager.onPacketReceived();
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
            MoneyStar.serverManager.update();
    }

    @Subscribe
    public void onWorldRender(Render3DEvent event) {
        MoneyStar.moduleManager.onRender3D(event);
    }

    @Subscribe public void onRenderGameOverlayEvent(Render2DEvent event) {
        MoneyStar.moduleManager.onRender2D(event);
    }

    @Subscribe public void onKeyInput(KeyEvent event) {
        MoneyStar.moduleManager.onKeyPressed(event.getKey());
    }

    @Subscribe public void onChatSent(ChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.cancel();
            try {
                if (event.getMessage().length() > 1) {
                    MoneyStar.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage(Formatting.RED + "An error occurred while running this command. Check the log!");
            }
        }
    }
}