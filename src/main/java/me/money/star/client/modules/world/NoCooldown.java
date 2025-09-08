package me.money.star.client.modules.world;

import me.money.star.client.System;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.util.traits.Util;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

public class NoCooldown extends Module {
    public Setting<Boolean> blockDelay = bool("ObsidianDelay", false);
    public Setting<Boolean> crystalDelay = bool("CrystalDelay", false);
    public Setting<Boolean> jumpDelay = bool("JumpDelay", false);
    public Setting<Boolean> hitDelay = bool("HitDelay", false);
    public Setting<Boolean> expDelay = bool("ExpDelay", false);


    public NoCooldown() {
        super("NoCooldown", Formatting.RED+"hahahah", Category.WORLD, true, false, false);
    }

    @Override public void onUpdate() {
        if (System.nullCheck()) return;

        if (expDelay.getValue() == true) {
            if (Util.mc.player.isHolding(Items.EXPERIENCE_BOTTLE)) {
                Util.mc.itemUseCooldown = 0;
            }
        }
        if (crystalDelay.getValue() == true) {
            if (Util.mc.player.isHolding(Items.END_CRYSTAL)) {
                Util.mc.itemUseCooldown = 0;
            }
        }
        if (blockDelay.getValue() == true) {
            if (Util.mc.player.isHolding(Items.OBSIDIAN)) {
                Util.mc.itemUseCooldown = 0;
            }
        }
    }
}
