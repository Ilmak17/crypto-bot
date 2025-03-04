package com.trading.bot.model.enums;

import lombok.Getter;

@Getter
public enum Topic {
    PRICE_UPDATES("price-updates"),
    ORDER_EVENTS("order-events"),
    TRADE_EXECUTIONS("trade-executions");

    private final String topicName;

    Topic(String topicName) {
        this.topicName = topicName;
    }

}
