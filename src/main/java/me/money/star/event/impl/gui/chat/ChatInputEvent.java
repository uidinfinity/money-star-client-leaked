package me.money.star.event.impl.gui.chat;


import me.money.star.event.Event;

/**
 * @author linus
 * @since 1.0
 */
public class ChatInputEvent extends Event {
    private final String chatText;

    public ChatInputEvent(String chatText) {
        this.chatText = chatText;
    }

    public String getChatText() {
        return chatText;
    }
}
