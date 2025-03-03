package com.trading.bot.model.enums;

public enum Topic {
    PRICE_UPDATES("price-updates"),
    ORDER_EVENTS("order-events"),
    TRADE_EXECUTIONS("trade-executions");

    private final String topicName;

    Topic(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
