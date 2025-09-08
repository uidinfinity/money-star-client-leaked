package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.commands.Command;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.entity.EntityDeathEvent;
import me.money.star.event.impl.world.AddEntityEvent;
import me.money.star.event.impl.world.RemoveEntityEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class Notifier extends Module {


    public Setting<Boolean> totemPop = bool("TotemPop", false);
    public Setting<Boolean> visualRange = bool("VisualRange", false);
    public Setting<Boolean> friends = bool("Friends", false);

    public Notifier() {
        super("Notifier", "Notifies in chat", Category.MISC,true,false,false);
    }

    @Subscribe
    public void onPacketInbound(PacketEvent event) {
        if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && totemPop.getValue()) {
            Entity entity = packet.getEntity(Util.mc.world);
            if (!(entity instanceof LivingEntity) || entity.getDisplayName() == null) {
                return;
            }
            int totems = MoneyStar.totemManager.getTotems(entity);
            String playerName = entity.getDisplayName().getString();
            boolean isFriend =  MoneyStar.friendManager.isFriend(playerName);
            if (isFriend && !friends.getValue() || entity == Util.mc.player) {
                return;
            }
            Command.sendMessage((isFriend ? "§b" : "§s") + playerName + "§f popped §s" + totems + "§f totems");
        }
    }

    @Subscribe
    public void onAddEntity(AddEntityEvent event) {
        if (!visualRange.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = MoneyStar.friendManager.isFriend(playerName);
        if (isFriend && !friends.getValue() || event.getEntity() == Util.mc.player) {
            return;
        }
        Command.sendMessage("§s[VisualRange] " + (isFriend ? "§b" + playerName : playerName) + "§f entered your visual range");
    }

    @Subscribe
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (!visualRange.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = MoneyStar.friendManager.isFriend(playerName);
        if (isFriend && !friends.getValue() || event.getEntity() == Util.mc.player) {
            return;
        }
        Command.sendMessage("§s[VisualRange] " + (isFriend ? "§b" + playerName : "§c" + playerName) + "§f left your visual range");
    }

    @Subscribe
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getDisplayName() == null) {
            return;
        }
        int totems = MoneyStar.totemManager.getTotems(event.getEntity());
        if (totems == 0) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = MoneyStar.friendManager.isFriend(playerName);
        if (isFriend && !friends.getValue() || event.getEntity() == Util.mc.player) {
            return;
        }
        Command.sendMessage((isFriend ? "§b" : "§s") + playerName + "§f died after popping §s" + totems + "§f totems");
    }
}
