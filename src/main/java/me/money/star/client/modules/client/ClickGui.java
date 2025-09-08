package me.money.star.client.modules.client;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.event.impl.ClientEvent;
import me.money.star.client.commands.Command;
import me.money.star.client.gui.MoneyStarGui;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ClickGui
        extends Module {
    private static ClickGui INSTANCE = new ClickGui();
    public Setting<String> prefix = str("Prefix", "$");

    private MoneyStarGui click;

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);

        this.setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                MoneyStar.commandManager.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to " + Formatting.DARK_GRAY + MoneyStar.commandManager.getPrefix());
            }
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        mc.setScreen(MoneyStarGui.getClickGui());
    }

    @Override
    public void onLoad() {
        MoneyStar.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof MoneyStarGui)) {
            this.disable();
        }
    }
    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
        mc.player.closeScreen();
    }
}