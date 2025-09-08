package me.money.star.client.modules.client;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.MoneyStarGui;
import me.money.star.client.gui.modules.ConcurrentModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.ClientColorEvent;
import me.money.star.event.impl.ClientEvent;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class Colors extends ConcurrentModule {
    private static Colors INSTANCE = new Colors();

    public Setting<Integer> red = num("Red", 142, 0, 255);
    public Setting<Integer> green = num("Green", 209, 0, 255);
    public Setting<Integer> blue = num("Blue", 138, 0, 255);
    public Setting<Boolean> rainbow = bool("Rainbow", false);

    public Setting<Integer> rainbowHue = num("Delay", 240, 0, 600);
    public Setting<Float> rainbowBrightness = num("Brightness ", 150.0f, 1.0f, 255.0f);
    public Setting<Float> rainbowSaturation = num("Saturation", 150.0f, 1.0f, 255.0f);
    private MoneyStarGui click;

    public Colors() {
        super("Colors","Color Setting", Category.CLIENT, true, false, true);

        rainbowHue.setVisibility(v -> rainbow.getValue());
        rainbowBrightness.setVisibility(v -> rainbow.getValue());
        rainbowSaturation.setVisibility(v -> rainbow.getValue());
        this.setInstance();
    }

    public static Colors getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Colors();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            MoneyStar.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(),255);
        }
    }



    @Override
    public void onLoad() {
        MoneyStar.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), 255);
    }




}