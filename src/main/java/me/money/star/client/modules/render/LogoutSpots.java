package me.money.star.client.modules.render;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import me.money.star.client.commands.Command;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.Render3DEvent;
import me.money.star.mixin.accessor.AccessorEntity;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.RenderUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

import java.awt.*;
import java.util.Map;
import java.util.UUID;

public class LogoutSpots extends Module {

    public enum Mode{
        Outline,Fill
    }
    //settings

    public Setting<Mode> mode = mode("Mode",Mode.Outline);
    public Setting<Double> line = num("LineWidth", 2.0,0.0,5.0);
    public Setting<Boolean> message = bool("Message",true);
    private final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutCache = Maps.newConcurrentMap();
    Color color = new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

    public LogoutSpots(){
        super("LogoutSpots","Show spots out",Category.RENDER,true,false,false);
    }
    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerListS2CPacket packet) {
            if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (PlayerListS2CPacket.Entry addedPlayer : packet.getPlayerAdditionEntries()) {
                    for (UUID uuid : logoutCache.keySet()) {
                        if (!uuid.equals(addedPlayer.profile().getId())) continue;
                        PlayerEntity player = logoutCache.get(uuid);
                        if (message.getValue()) Command.sendMessage("§f" + player.getName().getString() + " §rLogged back at §f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ());
                        logoutCache.remove(uuid);
                    }
                }
            }
            playerCache.clear();
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket packet) {
            for (UUID uuid2 : packet.profileIds()) {
                for (UUID uuid : playerCache.keySet()) {
                    if (!uuid.equals(uuid2)) continue;
                    final PlayerEntity player = playerCache.get(uuid);
                    if (!logoutCache.containsKey(uuid)) {
                        if (message.getValue()) Command.sendMessage("§f" + player.getName().getString() + " §rLogged out at §f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ());
                        logoutCache.put(uuid, player);
                    }
                }
            }
            playerCache.clear();
        }
    }

    @Override
    public void onEnable() {
        playerCache.clear();
        logoutCache.clear();
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;
            playerCache.put(player.getGameProfile().getId(), player);
        }
    }

    @Override
    public void onRender3D(Render3DEvent matrixStack) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            if (data == null) continue;
            if (mode.getValue() == Mode.Outline) {
                RenderUtil.drawBox(matrixStack.getMatrix(), ((AccessorEntity) data).getDimensions().getBoxAt(data.getPos()),Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) :color, line.getValue());
            }
            if (mode.getValue() == Mode.Fill) {
                RenderUtil.drawBox(matrixStack.getMatrix(), ((AccessorEntity) data).getDimensions().getBoxAt(data.getPos()),Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : color, line.getValue());
                RenderUtil.drawBoxFilled(matrixStack.getMatrix(), ((AccessorEntity) data).getDimensions().getBoxAt(data.getPos()),Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(),75));
            }

        }
    }
}
