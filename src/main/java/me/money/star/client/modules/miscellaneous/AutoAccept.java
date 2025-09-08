package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.PacketEvent;
import me.money.star.util.chat.ChatUtil;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;



public class AutoAccept extends Module {
    private final Timer acceptTimer = new CacheTimer();
    public Setting<Float> delay = num("Delay", 3.0f, 0f, 10.0f);

    public AutoAccept() {
        super("AutoAccept", "Automatically accepts teleport requests",
                Category.MISC,true,false,false);
    }

    @Subscribe
    public void onPacketInbound(PacketEvent event) {
        if (event.getPacket() instanceof ChatMessageS2CPacket packet) {
            String text = packet.body().content();
            if ((text.contains("has requested to teleport to you.")
                    || text.contains("has requested you teleport to them."))
                    && acceptTimer.passed(delay.getValue() * 1000)) {
                for (String friend : MoneyStar.friendManager.getFriends()) {
                    if (text.contains(friend)) {
                        ChatUtil.serverSendMessage("/tpaccept");
                        break;
                    }
                }
            }
        }
    }
}
