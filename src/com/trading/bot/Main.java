package com.trading.bot;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.model.enums.Symbol;
import com.trading.bot.service.ExchangerService;
import com.trading.bot.service.ExchangerServiceBean;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        ExchangerService exchangerService = new ExchangerServiceBean();
        exchangerService.initializeOrderBook(Symbol.BTCUSDT.toString(), 10);
    }
}
