package me.money.star.util.math.timer;

import com.google.common.eventbus.Subscribe;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.util.traits.Util;


/**
 * TODO: Test the accuracy of ticks
 *
 * @author linus
 * @see Timer
 * @since 1.0
 */
public class TickTimer implements Timer {
    //
    private long ticks;

    /**
     *
     */
    public TickTimer() {
        ticks = 0;
        Util.EVENT_BUS.register(this);
    }

    /**
     * @param event
     */
    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE) {
            ++ticks;
        }
    }

    /**
     * Returns <tt>true</tt> if the time since the last reset has exceeded
     * the param time.
     *
     * @param time The param time
     * @return <tt>true</tt> if the time since the last reset has exceeded
     * the param time
     */
    @Override
    public boolean passed(Number time) {
        return ticks >= time.longValue();
    }

    /**
     *
     */
    @Override
    public void reset() {
        setElapsedTime(0);
    }

    /**
     * @return
     */
    @Override
    public long getElapsedTime() {
        return ticks;
    }

    /**
     * @param time
     */
    @Override
    public void setElapsedTime(Number time) {
        ticks = time.longValue();
    }
}
