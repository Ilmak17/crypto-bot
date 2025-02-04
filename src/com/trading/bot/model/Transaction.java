package com.trading.bot.model;

import com.trading.bot.model.enums.OrderType;

@Deprecated
public record Transaction(OrderType type, double price, double amount, double total) {

    @Override
    public String toString() {
        return String.format("%s: %.6f BTC at %.2f USDT (Total: %.2f USDT)", type, amount, price, total);
    }
}
