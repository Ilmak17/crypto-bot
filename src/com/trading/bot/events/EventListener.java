package com.trading.bot.events;

public interface EventListener {
    void onEvent(String event, String message);
}
