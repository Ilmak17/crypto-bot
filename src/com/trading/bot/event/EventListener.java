package com.trading.bot.event;

public interface EventListener {
    void onEvent(String event, String message);
}
