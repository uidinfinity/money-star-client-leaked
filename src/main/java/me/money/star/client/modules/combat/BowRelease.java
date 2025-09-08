package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.UpdateEvent;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;


public class BowRelease extends Module {
    public Setting<Integer> ticks = num("Ticks", 3, 3, 20);

    public BowRelease() {
        super("BowRelease", "Automatically releases a charged bow",
                Category.COMBAT,true,false,false);
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if ((mc.player.getOffHandStack().getItem() == Items.BOW || mc.player.getMainHandStack().getItem() == Items.BOW) && mc.player.isUsingItem()) {
            if (mc.player.getItemUseTime() >= ticks.getValue().intValue()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                MoneyStar.networkManager.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                mc.player.stopUsingItem();
            }
        }
    }

}
