package me.money.star.util.render.animation;

public class TimeAnimation
{
    private double start;
    private double target;
    private float length;
    private long last = 0L;
    private boolean state;

    public TimeAnimation(double start, double target, float length)
    {
        this(false, start, target, length);
    }

    public TimeAnimation(boolean initial, double start, double target, float length)
    {
        this.start = start;
        this.target = target;
        this.length = length;
        this.state = initial;
    }

    public double getStart()
    {
        return start;
    }

    public void setStart(double start)
    {
        this.start = start;
    }

    public double getTarget()
    {
        return target;
    }

    public void setTarget(double target)
    {
        this.target = target;
    }

    public void setState(boolean state)
    {
        last = (long) (!state ? System.currentTimeMillis() - ((1 - getFactor()) * length) : System.currentTimeMillis() - (getFactor() * length));
        this.state = state;
    }

    public boolean getState()
    {
        return state;
    }

    public double getFactor()
    {
        return state ? clamp(((System.currentTimeMillis() - last) / length)) : clamp((1 - (System.currentTimeMillis() - last) / length));
    }

    public double getCurrent()
    {
        return start + ((target - start)) * getFactor();
    }

    private double clamp(double in)
    {
        return in < 0 ? 0 : Math.min(in, 1);
    }

    public double getLength()
    {
        return length;
    }

    public void setLength(float length)
    {
        this.length = length;
    }

    public boolean isFinished()
    {
        return !getState() && getFactor() == 0.0 || getState() && getFactor() == 1.0;
    }
}