package me.money.star.event.impl;


import me.money.star.event.Event;


public class ChatInputEvent extends Event
{
    private final String chatText;

    public ChatInputEvent(String chatText)
    {
        this.chatText = chatText;
    }

    public String getChatText()
    {
        return chatText;
    }
}
