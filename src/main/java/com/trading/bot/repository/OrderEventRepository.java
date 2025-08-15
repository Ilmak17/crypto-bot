package com.trading.bot.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class OrderEventRepository {

    private final CqlSession session;

    public void save(String symbol, String eventType, String payload) {
        PreparedStatement prepared = session.prepare(
                "INSERT INTO order_events (id, symbol, event_type, payload, created_at) " +
                        "VALUES (?, ?, ?, ?, ?);"
        );

        Optional.of(prepared.bind(
                UUID.randomUUID(),
                symbol,
                eventType,
                payload,
                Instant.now()
        )).ifPresent(session::execute);
    }

}
