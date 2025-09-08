package me.money.star.client.modules.movement;

import me.money.star.client.gui.modules.Module;
import me.money.star.mixin.accessor.AccessorAbstractBlock;
import me.money.star.util.traits.Util;
import net.minecraft.block.Blocks;

public class IceSpeed extends Module {


    public IceSpeed() {
        super("IceSpeed", "Modifies the walking speed on ice",
                Category.MOVEMENT,true,false,false);
    }

    @Override
    public void onEnable() {
        if (Util.mc.world == null) {
            return;
        }
        ((AccessorAbstractBlock) Blocks.ICE).setSlipperiness(0.4f);
        ((AccessorAbstractBlock) Blocks.PACKED_ICE).setSlipperiness(0.4f);
        ((AccessorAbstractBlock) Blocks.BLUE_ICE).setSlipperiness(0.4f);
        ((AccessorAbstractBlock) Blocks.FROSTED_ICE).setSlipperiness(0.4f);
    }

    @Override
    public void onDisable() {
        if (Util.mc.world == null) {
            return;
        }
        ((AccessorAbstractBlock) Blocks.ICE).setSlipperiness(0.98f);
        ((AccessorAbstractBlock) Blocks.PACKED_ICE).setSlipperiness(0.98f);
        ((AccessorAbstractBlock) Blocks.BLUE_ICE).setSlipperiness(0.98f);
        ((AccessorAbstractBlock) Blocks.FROSTED_ICE).setSlipperiness(0.98f);
    }
}
