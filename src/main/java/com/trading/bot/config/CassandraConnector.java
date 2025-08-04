package com.trading.bot.config;

import com.datastax.oss.driver.api.core.CqlSession;

import java.net.InetSocketAddress;

public class CassandraConnector {

    public static CqlSession connect() {
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("", 123))
                .withLocalDatacenter("datacenter1")
                .withKeyspace("trading")
                .build();
    }
}
