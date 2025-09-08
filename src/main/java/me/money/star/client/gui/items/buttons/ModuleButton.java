package me.money.star.client.gui.items.buttons;

import me.money.star.client.gui.Component;
import me.money.star.client.gui.items.Item;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.modules.client.Debug;
import me.money.star.client.settings.Bind;
import me.money.star.client.settings.Setting;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton
        extends Button {
    private final Module module;
    private List<Item> items = new ArrayList<>();
    private boolean subOpen;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.initSettings();
    }
    Color color = new Color( Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<>();
        if (!this.module.getSettings().isEmpty()) {
            for (Setting<?> setting : this.module.getSettings()) {
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton((Setting<Boolean>) setting));
                }
                if (setting.getValue() instanceof Bind && !setting.getName().equalsIgnoreCase("Bind:") && !this.module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton((Setting<Bind>) setting));
                }
                if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                    newItems.add(new StringButton((Setting<String>) setting));
                }
                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(new Slider((Setting<Number>) setting));
                    continue;
                }
                if (!setting.isEnumSetting()) continue;
                newItems.add(new EnumButton((Setting<Enum<?>>) setting));
            }
        }
        newItems.add(new BindButton((Setting<Bind>) this.module.getSettingByName("Bind:")));
        this.items = newItems;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(context, mouseX, mouseY, partialTicks);
        if (Debug.INSTANCE.gear.getValue()) drawString(this.subOpen ? Debug.INSTANCE.minus.getValue() : Debug.INSTANCE.plus.getValue() , this.x - 1.0f + (float) this.width - 8.0f, this.y + 4.0f,-1);
        if (!this.items.isEmpty()) {
            if (this.subOpen) {
                float height = 1.0f;
                for (Item item : this.items) {
                    Component.counter1[0] = Component.counter1[0] + 1;
                    if (!item.isHidden()) {
                        item.setLocation(this.x + 1.0f, this.y + (height += 15.0f));
                        item.setHeight(15);
                        item.setWidth(this.width - 9);
                        item.drawScreen(context, mouseX, mouseY, partialTicks);
                    }
                    if (item instanceof EnumButton && ((EnumButton)item).setting.open) {
                        height += ((EnumButton) item).setting.getValue().getClass().getEnumConstants().length * 12;
                    }
                    item.update();
                }
            }
        }
        if (Debug.INSTANCE.desc.getValue() && isHovering(mouseX,mouseY)){
            String description = Formatting.GRAY + module.getDescription();
            RenderUtil.rect(context.getMatrices(),1, mc.currentScreen.height - 11, mc.textRenderer.getWidth(description) + 2, mc.currentScreen.height, new Color(-1072689136).getRGB());

            RenderUtil.rect2(context.getMatrices(),1, mc.currentScreen.height - 11, mc.textRenderer.getWidth(description) + 2, mc.currentScreen.height, Debug.INSTANCE.outline.getValue()&&  Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.toARGB(Colors.getInstance().red.getValue(),Colors.getInstance().green.getValue(),Colors.getInstance().blue.getValue(),Debug.INSTANCE.outline.getValue()?255:0),1);

            assert mc.currentScreen != null;
            drawString(description, 2, mc.currentScreen.height - 9, -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                this.subOpen = !this.subOpen;
                mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
            if (this.subOpen) {
                for (Item item : this.items) {
                    if (item.isHidden()) continue;
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override public void onKeyPressed(int key) {
        super.onKeyPressed(key);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyPressed(key);
            }
        }
    }

    @Override
    public int getHeight() {
        if (this.subOpen) {
            int height = 14;
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                height += item.getHeight() + 1;
            }
            return height + 2;
        }
        return 14;
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }

    @Override
    public boolean getState() {
        return this.module.isEnabled();
    }
}