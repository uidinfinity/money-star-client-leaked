package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.BreakBlockEvent;
import me.money.star.event.impl.network.InteractBlockEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

public class NoGlitchBlocks extends Module {

    public Setting<Boolean> place = bool("Place", false);
    public Setting<Boolean> destroy = bool("Destroy", false);
    public NoGlitchBlocks() {
        super("NoGlitchBlocks", "Prevents blocks from being glitched in the world",
                Category.WORLD,true,false,false);
    }

    /**
     * @param event
     */
    @Subscribe
    public void onInteractBlock(InteractBlockEvent event) {
        if (place.getValue() && !Util.mc.isInSingleplayer()) {
            event.cancel();
            MoneyStar.networkManager.sendSequencedPacket(id ->
                    new PlayerInteractBlockC2SPacket(event.getHand(), event.getHitResult(), id));
        }
    }

    /**
     * @param event
     */
    @Subscribe
    public void onBreakBlock(BreakBlockEvent event) {
        if (destroy.getValue() && !Util.mc.isInSingleplayer()) {
            event.cancel();
            BlockState state = Util.mc.world.getBlockState(event.getPos());
            state.getBlock().onBreak(Util.mc.world, event.getPos(), state, Util.mc.player);
        }
    }
}
