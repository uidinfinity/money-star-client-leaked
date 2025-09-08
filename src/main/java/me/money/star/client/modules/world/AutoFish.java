package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.util.traits.IMinecraftClient;
import me.money.star.util.traits.Util;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;



public class AutoFish extends Module {
    //
    public Setting<Boolean> openInventory = bool("OpenInventory", true);
    public Setting<Integer> castDelay = num("CastingDelay", 15, 10, 25);

    public Setting<Float> maxSoundDist = num("MaxSoundDist", 2.0f, 0f, 5.0f);


    private boolean autoReel;
    private int autoReelTicks;
    private int autoCastTicks;

    /**
     *
     */
    public AutoFish() {
        super("AutoFish", "Automatically casts and reels fishing rods",
               Category.WORLD,true,false,false);
    }

    @Subscribe
    public void onPacketInbound(PacketEvent event) {
        if (Util.mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof PlaySoundS2CPacket packet
                && packet.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH
                && Util.mc.player.getMainHandStack().getItem() == Items.FISHING_ROD) {
            FishingBobberEntity fishHook = Util.mc.player.fishHook;
            if (fishHook == null || fishHook.getPlayerOwner() != Util.mc.player) {
                return;
            }
            double dist = fishHook.squaredDistanceTo(packet.getX(),
                    packet.getY(), packet.getZ());
            if (dist <= maxSoundDist.getValue()) {
                autoReel = true;
                autoReelTicks = 4;
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() != Stage.PRE) {
            return;
        }
        if (Util.mc.currentScreen == null || Util.mc.currentScreen instanceof ChatScreen
                || openInventory.getValue()) {
            if (Util.mc.player.getMainHandStack().getItem() != Items.FISHING_ROD) {
                return;
            }
            FishingBobberEntity fishHook = Util.mc.player.fishHook;
            if ((fishHook == null || fishHook.getHookedEntity() != null)
                    && autoCastTicks <= 0) {
                ((IMinecraftClient) Util.mc).rightClick();
                autoCastTicks = castDelay.getValue();
                return;
            }
            if (autoReel) {
                if (autoReelTicks <= 0) {
                    ((IMinecraftClient) Util.mc).rightClick();
                    autoReel = false;
                    return;
                }
                autoReelTicks--;
            }
        }
        autoCastTicks--;
    }
}
