package com.trading.bot.bots;

public interface Bot {
    void performAction(double price);

    void getBalance();
}
