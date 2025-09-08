package me.money.star.client.modules.client;

import me.money.star.client.gui.modules.Module;
import net.minecraft.util.Identifier;


public class Capes extends Module {
    public Capes() {
        super("Capes", "Adds a cape that only you can see.", Category.CLIENT, true, false, false);
        this.capeTexture = Identifier.of("assets/money/star", "textures/capes/chery.png");

    }
    private final Identifier capeTexture;
    public Identifier getCapeTexture() {
        return this.capeTexture;
    }
}