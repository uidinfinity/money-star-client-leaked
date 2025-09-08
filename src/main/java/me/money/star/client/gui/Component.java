package me.money.star.client.gui;

import me.money.star.client.System;
import me.money.star.client.gui.items.Item;
import me.money.star.client.gui.items.buttons.Button;
import me.money.star.client.modules.client.Colors;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Component
        extends System {

    public static int[] counter1 = new int[]{1};
    protected DrawContext context;
    private final List<Item> items = new ArrayList<>();
    public boolean drag;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private int width;
    private int height;
    private float heightFloat;
    private int heightOutline;
    private boolean open;
    private boolean hidden = false;

    public Component(String name, int x, int y, boolean open) {
        super(name);
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 18;
        this.heightFloat = 18;
        this.heightOutline = 8;
        this.open = open;
        this.setupItems();
    }

    public void setupItems() {
    }

    private void drag(int mouseX, int mouseY) {
        if (!this.drag) {
            return;
        }
        this.x = this.x2 + mouseX;
        this.y = this.y2 + mouseY;
    }
    int color = ColorUtil.toARGB(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

    public void drawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);
        counter1 = new int[]{1};
        float totalItemHeight = this.open ? this.getTotalItemHeight() - 2.0f : 0.0f;

        context.fill(this.x, this.y - 1, this.x + this.width, this.y + this.height - 4, Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()).getRGB() : color);
        if (this.open) {
            RenderUtil.rect(context.getMatrices(), this.x, (float) this.y + 12.5f, this.x + this.width, (float) (((float) (this.y + this.height) + totalItemHeight )), 0x77000000);
            drawOutline(color);

        }
        drawString(this.getName(), (float) this.x + 3.0f, (float) this.y - 4.0f - (float) MoneyStarGui.getClickGui().getTextOffset(), -1);
        if (this.open) {
            float y = (float) (this.getY() + this.getHeight()) - 3.0f;
            for (Item item : this.getItems()) {
                Component.counter1[0] = counter1[0] + 1;
                if (item.isHidden()) continue;
                item.setLocation((float) this.x + 2.0f, y);
                item.setWidth(this.getWidth() - 4);
                item.drawScreen(context, mouseX, mouseY, partialTicks);
                y += (float) item.getHeight() + 1.5f;
            }
        }
    }
    private void drawOutline( int color) {
        float totalItemHeight = 0.0f;

        if (open) {
            totalItemHeight = getTotalItemHeight() - 2.0f;
        }
        RenderUtil.drawLine(x, (float) y - 1.5f, x, (float) (y + height) + totalItemHeight, color);
        RenderUtil.drawLine(x + width, (float) y - 1.5f, x + width, (float) (y + height) + totalItemHeight, color);
        RenderUtil.drawLine(x, (float) y - 1.5f, x + width, (float) y - 1.5f, color);
        RenderUtil.drawLine(x, (float) (y + height) + totalItemHeight, x + width, (float) (y + height) + totalItemHeight, color);
    }






    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            MoneyStarGui.getClickGui().getComponents().forEach(component -> {
                if (component.drag) {
                    component.drag = false;
                }
            });
            this.drag = true;
            return;
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            return;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            this.drag = false;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void onKeyPressed(int key) {
        if (!open) return;
        this.getItems().forEach(item -> item.onKeyPressed(key));
    }

    public void addButton(Button button) {
        this.items.add(button);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOpen() {
        return this.open;
    }

    public final List<Item> getItems() {
        return this.items;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - (this.open ? 2 : 0);
    }

    private float getTotalItemHeight() {
        float height = 0.0f;
        for (Item item : this.getItems()) {
            height += (float) item.getHeight() + 1.5f;
        }
        return height;
    }

    protected void drawString(String text, double x, double y, Color color) {
        drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        context.drawTextWithShadow(mc.textRenderer, text, (int) x, (int) y, color);
    }
}