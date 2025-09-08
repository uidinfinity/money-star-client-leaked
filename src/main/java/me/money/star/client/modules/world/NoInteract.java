package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.System;
import me.money.star.client.gui.modules.Module;
import me.money.star.event.impl.PacketEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;


public class NoInteract extends Module {

    public NoInteract(){
        super("NoInteract","don't opening",Category.WORLD,true,false,false);
    }
    @Subscribe
    public void onPacket(PacketEvent.Send event) {
        if (System.nullCheck() || !(event.getPacket() instanceof PlayerInteractBlockC2SPacket packet)) {
            return;
        }
        Block block = Util.mc.world.getBlockState(packet.getBlockHitResult().getBlockPos()).getBlock();
        if (!Util.mc.player.isSneaking()) {
            if (block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof AnvilBlock) {
                event.cancel();
            }
        }
    }
}
