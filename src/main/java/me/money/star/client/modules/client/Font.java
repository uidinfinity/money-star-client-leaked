package me.money.star.client.modules.client;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.ConcurrentModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.ClientEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.util.render.font.Fonts;


public class Font extends ConcurrentModule {
    public static Font INSTANCE = new Font();
    public Setting<Boolean> antiAlias = bool("Descriptions", false);
    public Setting<Boolean> fractionalMetrics  = bool("Outline", false);
    public Setting<Integer> size = num("Delay", 9, 5, 12);
    public Setting<Float> vanillaShadow = num("VanillaShadow", 1.0f, 0.1f, 15.0f);

    public Font() {
        super("Fonts", "Time new roman", Category.CLIENT, true, false, false);
        INSTANCE = this;
    }
    public static Font getInstance()
    {
        return INSTANCE;
    }
    @Subscribe
    public void onTick(TickEvent event)
    {
        if (event.getStage() == Stage.PRE && Fonts.FONT_SIZE != size.getValue())
        {
            Fonts.setSize(size.getValue());
        }
    }

    @Subscribe
    public void onClientUpdate(ClientEvent event)
    {
        if (!Fonts.isInitialized())
        {
            return;
        }

        if ((event.getSetting() == antiAlias || event.getSetting() == fractionalMetrics))
        {
            Fonts.closeFonts();
        }
    }

    public boolean getAntiAlias()
    {
        return antiAlias.getValue();
    }

    public boolean getFractionalMetrics()
    {
        return fractionalMetrics.getValue();
    }

    public float getVanillaShadow()
    {
        return vanillaShadow.getValue();
    }
}
