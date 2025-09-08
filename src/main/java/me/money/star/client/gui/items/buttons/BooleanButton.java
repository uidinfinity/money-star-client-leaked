package me.money.star.client.gui.items.buttons;


import me.money.star.MoneyStar;
import me.money.star.client.gui.MoneyStarGui;
import me.money.star.client.modules.client.ClickGui;
import me.money.star.client.settings.Setting;

import me.money.star.util.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;

public class BooleanButton
        extends Button {
    private final Setting<Boolean> setting;

    public BooleanButton(Setting<Boolean> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        RenderUtil.rect(context.getMatrices(), this.x, this.y, this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? MoneyStar.colorManager.getColorWithAlpha(255) : MoneyStar.colorManager.getColorWithAlpha(255)) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        drawString(this.getName(), this.x + 2.3f, this.y - 1.7f - (float) MoneyStarGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        if (setting.parent) {

            String color = "" + Formatting.WHITE;
            String gear = setting.open ? "-" : "+";

            drawString(color + gear, x - 1.5f + (float) width - 7.4f + 8.0f, y - 2.2f - (float) MoneyStarGui.getInstance().getTextOffset(), -1);
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
        this.setting.setValue(!this.setting.getValue());
    }

    @Override
    public boolean getState() {
        return this.setting.getValue();
    }
}