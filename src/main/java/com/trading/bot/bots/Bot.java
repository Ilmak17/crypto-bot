package com.trading.bot.bots;

import com.trading.bot.model.Order;
import java.util.List;

public interface Bot {
    void performAction(double price);

    void getBalance();

    List<Order> getOrderHistory();

    Double getUsdtBalance();

    Double getBtcBalance();
}
