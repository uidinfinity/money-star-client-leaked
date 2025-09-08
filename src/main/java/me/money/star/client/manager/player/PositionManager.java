package me.money.star.client.manager.player;

import com.google.common.eventbus.Subscribe;
import me.money.star.event.Stage;
import me.money.star.event.impl.UpdateWalkingPlayerEvent;
import me.money.star.client.System;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PositionManager
        extends System {
    private double x;
    private double y;
    private double z;
    private boolean onground;
    private double fallDistance;

    public PositionManager() {
        Util.EVENT_BUS.register(this);
    }

    @Subscribe public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == Stage.POST) return;

        double diff = Util.mc.player.prevY - Util.mc.player.getY();
        if (Util.mc.player.isOnGround() || diff <= 0) {
            fallDistance = 0;
        } else {
            fallDistance += diff;
        }
    }

    public void updatePosition() {
        this.x = Util.mc.player.getX();
        this.y = Util.mc.player.getY();
        this.z = Util.mc.player.getZ();
        this.onground = Util.mc.player.isOnGround();
    }

    public void restorePosition() {
        Util.mc.player.setPosition(x, y, z);
        Util.mc.player.setOnGround(onground);
    }

    public void setPlayerPosition(double x, double y, double z) {
        Util.mc.player.setPosition(x, y, z);
    }

    public void setPlayerPosition(double x, double y, double z, boolean onground) {
        Util.mc.player.setPosition(x, y, z);
        Util.mc.player.setOnGround(onground);
    }

    public void setPositionPacket(double x, double y, double z, boolean onGround, boolean setPos, boolean noLagBack) {
        boolean bl = Util.mc.player.horizontalCollision;
        Util.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, bl));
        if (setPos) {
            Util.mc.player.setPosition(x, y, z);
            if (noLagBack) {
                this.updatePosition();
            }
        }
    }
    public void setPositionClient(double x, double y, double z)
    {
        if (mc.player.isRiding())
        {
            mc.player.getVehicle().setPosition(x, y, z);
            return;
        }
        mc.player.setPosition(x, y, z);
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getFallDistance() {
        return fallDistance;
    }
}