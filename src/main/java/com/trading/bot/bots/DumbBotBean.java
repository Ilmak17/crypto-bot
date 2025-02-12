package com.trading.bot.bots;


import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.service.ExchangerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DumbBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final Random random;
    private final List<Order> orderHistory;
    private ExchangerService exchangerService;

    public DumbBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        random = new Random();
        orderHistory = new ArrayList<>();
    }

    @Override
    public void performAction(double price) {
        switch (random.nextInt(3)) {
            case 0 -> System.out.println(name + " decided to skip.");
            case 1 -> placeOrder(price, OrderType.BUY);
            case 2 -> placeOrder(price, OrderType.SELL);
            default -> System.out.println("Error has occurred");
        }
    }

    @Override
    public void getBalance() {
        System.out.printf("%s Balance: %.2f USDT | %.6f BTC%n", name, usdtBalance, btcBalance);
    }

    @Override
    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    @Override
    public Double getUsdtBalance() {
        return usdtBalance;
    }

    @Override
    public Double getBtcBalance() {
        return btcBalance;
    }

    @Override
    public void setExchangerService(ExchangerService exchangerService) {
        this.exchangerService = exchangerService;
    }

    private void placeOrder(double price, OrderType type) {
        double amount;

        if (type == OrderType.BUY) {
            amount = random.nextDouble(usdtBalance / price);
            if (amount <= 0) return;
            usdtBalance -= amount * price;
            btcBalance += amount;
        } else {
            amount = random.nextDouble(btcBalance);
            if (amount <= 0) return;
            btcBalance -= amount;
            usdtBalance += amount * price;
        }

        Order order = new Order(
                System.nanoTime(), type, amount, price, OrderStatus.NEW, null
        );

        orderHistory.add(order);
        exchangerService.placeOrder(order);
        System.out.printf("%s placed %s order: %.6f BTC @ %.2f%n", name, type, amount, price);
    }
}
