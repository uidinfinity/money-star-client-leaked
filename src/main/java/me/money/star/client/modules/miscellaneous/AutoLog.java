package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.util.traits.Util;
import me.money.star.util.world.FakePlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.text.Text;



public class AutoLog extends Module {
    public Setting<Float> healthPlayer = num("Health ", 5f, 0.1f, 19f);
    public Setting<Boolean> healthTotem = bool("HealthTotems", true);
    public Setting<Boolean> onRender = bool("OnRender", false);
    public Setting<Boolean> noTotem = bool("NoTotems", false);
    public Setting<Integer> totemsPlayer = num("Totems", 1, 0, 5);
    public Setting<Boolean> illegalDisconnect = bool("IllegalDisconnect", false);
    public AutoLog() {
        super("AutoLog", "Automatically disconnects from server during combat", Category.MISC,true,false,false);
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() != Stage.PRE) {
            return;
        }
        if (onRender.getValue()) {
            AbstractClientPlayerEntity player = Util.mc.world.getPlayers().stream()
                    .filter(p -> checkEnemy(p)).findFirst().orElse(null);
            if (player != null) {
                playerDisconnect("[AutoLog] %s came into render distance.", player.getName().getString());
                return;
            }
        }
        float health = Util.mc.player.getHealth() + Util.mc.player.getAbsorptionAmount();
        int totems = MoneyStar.inventoryManager.count(Items.TOTEM_OF_UNDYING);
        boolean b2 = totems <= totemsPlayer.getValue();
        if (health <= healthPlayer.getValue()) {
            if (!healthTotem.getValue()) {
                playerDisconnect("[AutoLog] logged out with %d hearts remaining.", (int) health);
                return;
            } else if (b2) {
                playerDisconnect("[AutoLog] logged out with %d totems and %d hearts remaining.", totems, (int) health);
                return;
            }
        }
        if (b2 && noTotem.getValue()) {
            playerDisconnect("[AutoLog] logged out with %d totems remaining.", totems);
        }
    }

    /**
     * @param disconnectReason
     * @param args
     */
    private void playerDisconnect(String disconnectReason, Object... args) {
        if (illegalDisconnect.getValue()) {
            MoneyStar.networkManager.sendPacket(PlayerInteractEntityC2SPacket.attack(Util.mc.player, false)); // Illegal packet
            disable();
            return;
        }
        if (Util.mc.getNetworkHandler() == null) {
            Util.mc.world.disconnect();
            disable();
            return;
        }
        disconnectReason = String.format(disconnectReason, args);
        Util.mc.getNetworkHandler().getConnection().disconnect(Text.of(disconnectReason));
        disable();
    }

    private boolean checkEnemy(AbstractClientPlayerEntity player) {
        return player.getDisplayName() != null && !MoneyStar.friendManager.isFriend(getName()) && !(player instanceof FakePlayerEntity);
    }
}
