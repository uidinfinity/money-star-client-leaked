package me.money.star.client.modules.world;


import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;


public class NoHitBox extends Module {
    public NoHitBox(){
        super("NoHitBox","i miss",Category.WORLD,true,false,false);
    }

    public Setting<Boolean> ponly = bool("Pickaxe-Only", false);
    public Setting<Boolean> noSword = bool("No-Sword", false);
}
