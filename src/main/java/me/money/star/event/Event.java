package me.money.star.event;

public class Event {
    private final boolean cancelable =
            getClass().isAnnotationPresent(Cancelable.class);
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }
    public boolean isCancelable() {
        return cancelable;
    }
    public void setCancelled(boolean cancel) {
        if (isCancelable()) {
            cancelled = cancel;
        }
    }

}
