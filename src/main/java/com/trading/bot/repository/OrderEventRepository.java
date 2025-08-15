package com.trading.bot.repository;

import com.datastax.oss.driver.api.core.CqlSession;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class OrderEventRepository {

    private final CqlSession session;

    public OrderEventRepository(CqlSession session) {
        this.session = session;
    }

    public void save(String symbol, String eventType, String payload) {
        Optional.of(session)
                .map(s -> s.prepare(
                        "INSERT INTO order_events (id, symbol, event_type, payload, created_at) " +
                                "VALUES (?, ?, ?, ?, ?);"))
                .map(prepared -> prepared.bind(
                        UUID.randomUUID(),
                        symbol,
                        eventType,
                        payload,
                        Instant.now()))
                .ifPresent(session::execute);
    }

}
