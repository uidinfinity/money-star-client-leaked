package me.money.star.event.impl.render;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class CameraClipEvent extends Event {
        private float distance;

    public CameraClipEvent(float distance)
        {
            this.distance = distance;
        }

        public float getDistance()
        {
            return distance;
        }

        public void setDistance(float distance)
        {
            this.distance = distance;
        }
    }
