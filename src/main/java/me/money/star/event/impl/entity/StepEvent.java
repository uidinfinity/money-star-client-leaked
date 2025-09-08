package me.money.star.event.impl.entity;


import me.money.star.event.Event;

public class StepEvent extends Event {
    private final double stepHeight;

    public StepEvent(double stepHeight) {
        this.stepHeight = stepHeight;
    }

    public double getStepHeight() {
        return stepHeight;
    }
}
