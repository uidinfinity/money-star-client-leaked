package me.money.star.client.modules.client;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.ConcurrentModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.UpdateEvent;
import me.money.star.mixin.accessor.AccessorPlayerMoveC2SPacket;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.DirectionUtil;
import me.money.star.util.world.BlastResistantBlocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;

public class AntiCheat extends ConcurrentModule {
    private static AntiCheat INSTANCE;

    public enum Anticheats {
    Grim, NCP, Vanilla,
    }
    public Setting<Anticheats> mode = mode("Mode", Anticheats.Vanilla);

    public Setting<Boolean> miningFix = bool("Mining-Fix", false);
    public Setting<Boolean> webJumpFix = bool("Web-Jump-Fix", false);
    public Setting<Boolean> raytraceSpoof = bool("Raytrace-Fix", false);
    public Setting<Boolean> packetKick = bool("No-Packet-Kick", false);
    private final Timer raytraceTimer = new CacheTimer();
    private float pitch = Float.NaN;


    public AntiCheat() {
        super("AntiCheat", "Anti-cheat settings.", Category.CLIENT, true, false, false);
    INSTANCE = this;
}

    public static AntiCheat getInstance()
    {
        return INSTANCE;
    }

    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (isNCP() && raytraceSpoof.getValue() && event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && raytraceTimer.passed(250))
        {
            BlockHitResult packetResult = packet.getBlockHitResult();
            BlockPos pos = packetResult.getBlockPos();
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), DirectionUtil.getDirectionOffsetPos(pos, packetResult.getSide()),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (mc.world.isSpaceEmpty(mc.player.getBoundingBox().stretch(0.0, 0.15, 0.0)) && result != null
                    && result.getType() == HitResult.Type.BLOCK && !result.getBlockPos().equals(pos))
            {
                pitch = -75;
                raytraceTimer.reset();
            }
        }

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook() && !Float.isNaN(pitch))
        {
            ((AccessorPlayerMoveC2SPacket) packet).hookSetPitch(pitch);
            pitch = Float.NaN;
        }

        if (isGrim() && miningFix.getValue() && event.getPacket() instanceof PlayerActionC2SPacket packet
                && (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                || packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                || packet.getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK))
        {
            if (BlastResistantBlocks.isUnbreakable(packet.getPos()))
            {
                event.cancel();
                return;
            }

            if (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK)
            {
                MoneyStar.networkManager.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                MoneyStar.networkManager.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                MoneyStar.networkManager.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                event.cancel();
            }
        }
    }

    @Subscribe
    public void onUpdate(UpdateEvent event)
    {
        if (!raytraceSpoof.getValue())
        {
            pitch = Float.NaN;
        }
    }

    public boolean isGrim()
    {
        return mode.getValue() == Anticheats.Grim;
    }

    public boolean isNCP()
    {
        return mode.getValue() == Anticheats.NCP;
    }

    public boolean getMiningFix()
    {
        return miningFix.getValue();
    }

    public boolean getWebJumpFix()
    {
        return webJumpFix.getValue();
    }
    public boolean isPacketKick()
    {
        return packetKick.getValue();
    }
}
