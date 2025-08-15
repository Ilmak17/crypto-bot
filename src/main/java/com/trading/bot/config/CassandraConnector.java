package com.trading.bot.config;

import com.datastax.oss.driver.api.core.CqlSession;

import java.net.InetSocketAddress;

public class CassandraConnector {
    private CqlSession session;

    public void connect(String node, int port, String datacenter, String keyspace) {
        this.session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(node, port))
                .withLocalDatacenter(datacenter)
                .withKeyspace(keyspace)
                .build();
    }

    public CqlSession getSession() {
        return session;
    }

    public void close() {
        session.close();
    }
}
