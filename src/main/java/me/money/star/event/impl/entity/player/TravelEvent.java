package me.money.star.event.impl.entity.player;

import me.money.star.event.Cancelable;
import me.money.star.event.StageEvent;
import net.minecraft.util.math.Vec3d;


@Cancelable
public class TravelEvent extends StageEvent {
    private  Vec3d movementInput;

    public TravelEvent(Vec3d movementInput) {
        this.movementInput = movementInput;
    }

    public Vec3d getMovementInput() {
        return movementInput;
    }

    private Vec3d mVec;
    private boolean pre;

    public TravelEvent(Vec3d mVec, boolean pre) {
        this.mVec = mVec;
        this.pre = pre;
    }

    public Vec3d getmVec() {
        return mVec;
    }

    public boolean isPre() {
        return pre;
    }
}

