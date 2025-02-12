package com.trading.bot.bots;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.service.ExchangerService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class SmartBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final List<Order> orderHistory;
    private final Queue<Double> priceHistory;
    private static final Double COMMISSION = 0.001;
    private final Random random;
    private ExchangerService exchangerService;

    public SmartBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        orderHistory = new ArrayList<>();
        priceHistory = new LinkedList<>();
        random = new Random();
    }

    @Override
    public void performAction(double price) {
        if (priceHistory.size() == 5) {
            priceHistory.poll();
        }
        priceHistory.offer(price);

        double tradeVolume = random.nextDouble(5000);
        boolean isTrendingUp = isTrendingUp(priceHistory);

        if (!isTrendingUp && usdtBalance > 0 && tradeVolume > 2500) {
            placeOrder(price, OrderType.BUY);
        } else if (isTrendingUp && btcBalance > 0 && tradeVolume > 2500) {
            placeOrder(price, OrderType.SELL);
        } else {
            System.out.println(name + " decided to skip.");
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
        double amount = (type == OrderType.BUY) ? usdtBalance * 0.5 / price : btcBalance * 0.5;
        if (amount <= 0) return;

        if (type == OrderType.BUY) {
            usdtBalance -= amount * price;
            btcBalance += amount * (1 - COMMISSION);
        } else {
            btcBalance -= amount;
            usdtBalance += amount * price * (1 - COMMISSION);
        }

        Order order = new Order(
                System.nanoTime(), type, amount, price, OrderStatus.NEW, null
        );

        orderHistory.add(order);
        exchangerService.placeOrder(order);
        System.out.printf("%s placed %s order: %.6f BTC @ %.2f%n", name, type, amount, price);
    }

    private boolean isTrendingUp(Queue<Double> priceHistory) {
        if (priceHistory.size() < 2) return false;

        double[] prices = priceHistory.stream().mapToDouble(Double::doubleValue).toArray();
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] <= prices[i - 1]) return false;
        }
        return true;
    }
}
