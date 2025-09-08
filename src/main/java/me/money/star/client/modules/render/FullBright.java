package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.ClientEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.network.GameJoinEvent;
import me.money.star.event.impl.render.LightmapGammaEvent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBright extends Module {

    public Setting<Mode> mode = mode("Mode", Mode.GAMMA);


    public FullBright() {
        super("FullBright", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
    }
    @Override
    public void onEnable()
    {
        if (mc.player != null && mc.world != null
                && mode.getValue() == Mode.POTION)
        {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0)); // INFINITE
        }
    }

    @Override
    public void onDisable()
    {
        if (mc.player != null && mc.world != null
                && mode.getValue() == Mode.POTION)
        {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @Subscribe
    public void onGameJoin(GameJoinEvent event)
    {
        onDisable();
        onEnable();
    }



    @Subscribe
    public void onSettingChange(ClientEvent event)
    {
        if (mc.player != null && mode == event.getSetting() && event.getStage() == Stage.POST.hashCode() && mode.getValue() != Mode.POTION)
        {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (mode.getValue() == Mode.POTION
                && !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
        {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0));
        }
    }


    public enum Mode
    {
        GAMMA,
        POTION
    }
}
