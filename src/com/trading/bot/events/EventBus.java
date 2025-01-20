package com.trading.bot.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventBus {
    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    public void subscribe(String event, EventListener listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public void publish(String event, String message) {
        Optional.ofNullable(listeners.get(event))
                .ifPresent(eventListeners ->
                        eventListeners.forEach(listener -> listener.onEvent(event, message)));
    }
}
