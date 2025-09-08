package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.RotationModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.UpdateEvent;
import me.money.star.event.impl.network.GameJoinEvent;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import net.minecraft.client.option.KeyBinding;


public class NoAFK extends RotationModule {
    public Setting<Boolean> jump = bool("Jump", false);
    public Setting<Boolean> sneak = bool("Sneak", false);
    public Setting<Boolean> rotate = bool("Rotate", false);
    public Setting<Boolean> message = bool("Message", false);
    public Setting<String> text = str("Prefix", "I'm definitely not AFK :^)");


    private final Timer afkTimer = new CacheTimer();
    private final Timer actionTimer = new CacheTimer();
    public NoAFK() {
        super("NoAFK", "NoAFK",
                Category.MISC,true,false,false);
    }
    @Override
    public void onEnable()
    {
        afkTimer.reset();
    }

    @Subscribe
    public void onGameJoin(GameJoinEvent event)
    {
        afkTimer.reset();
    }

    @Subscribe
    public void onUpdate(UpdateEvent event)
    {
        for (KeyBinding keyBinding : mc.options.allKeys)
        {
            if (keyBinding.isPressed())
            {
                afkTimer.reset();
                break;
            }
        }
        {
            if (sneak.getValue()) {
                if (nullCheck())
                    return;
                mc.options.sneakKey.setPressed(true);
            }
            if (jump.getValue()) {
                if (nullCheck())
                    return;
                mc.options.jumpKey.setPressed(true);
            }
            if (rotate.getValue())
            {
                setRotationClient(mc.player.getYaw() + (RANDOM.nextFloat(90.0f) * (RANDOM.nextBoolean() ? 1.0f : -1.0f)),
                        mc.player.getPitch() + (RANDOM.nextFloat(90.0f) * (RANDOM.nextBoolean() ? 1.0f : -1.0f)));
            }
            if (message.getValue() && afkTimer.passed(4200d)) {
                mc.player.networkHandler.sendChatMessage(text.getValue());
                afkTimer.reset();
            }
            actionTimer.reset();
        }
    }



}
