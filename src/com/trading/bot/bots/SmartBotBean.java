package com.trading.bot.bots;

import com.trading.bot.model.OrderType;
import com.trading.bot.model.Transaction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class SmartBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final List<Transaction> transactions;
    private final Queue<Double> priceHistory;
    private static final Double COMMISSION = 0.001;
    private final Random random;

    public SmartBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        transactions = new ArrayList<>();
        priceHistory = new LinkedList<>();
        random = new Random();
    }

    @Override
    public void performAction(double price) {
        if (priceHistory.size() == 5) {
            priceHistory.poll();
        }
        priceHistory.offer(price);


        // volume in market
        double tradeVolume = random.nextDouble(5000);

        if (isTrendingDown() && usdtBalance > 0 && tradeVolume > 2500) {
            buy(price);
        } else if (isTrendingUp() && btcBalance > 0 && tradeVolume > 2500) {
            sell(price);
        } else {
            System.out.println(name + " decided to skip.");
        }
    }

    @Override
    public void getBalance() {
        System.out.printf("%s Balance: %.2f USDT | %.6f BTC%n", name, usdtBalance, btcBalance);
    }

    @Override
    public void getTransactionHistory() {
        System.out.println(name + " Transaction History:");
        transactions.forEach(System.out::println);
    }

    private void buy(double price) {
        double spendAmount = usdtBalance * 0.5;
        if (spendAmount > 0) {
            double btcBought = spendAmount / price;
            btcBought *= (1 - COMMISSION);
            usdtBalance -= spendAmount;
            btcBalance += btcBought;

            transactions.add(new Transaction(OrderType.BUY, price, btcBought, spendAmount));
            System.out.printf("%s bought %.6f BTC for %.2f USDT.%n", name, btcBought, spendAmount);
        }
    }

    private void sell(double price) {
        double btcToSell = btcBalance * 0.5;
        if (btcToSell > 0) {
            double earnedUsdt = btcToSell * price;
            earnedUsdt *= (1 - COMMISSION);
            btcBalance -= btcToSell;
            usdtBalance += earnedUsdt;

            transactions.add(new Transaction(OrderType.SELL, price, btcToSell, earnedUsdt));
            System.out.printf("%s sold %.6f BTC for %.2f USDT.%n", name, btcToSell, earnedUsdt);
        }
    }

    private boolean isTrendingUp() {
        if (priceHistory.size() < 2) return false;

        double[] prices = priceHistory.stream().mapToDouble(Double::doubleValue).toArray();
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] <= prices[i - 1]) return false;
        }

        return true;
    }

    private boolean isTrendingDown() {
        if (priceHistory.size() < 2) return false;

        double[] prices = priceHistory.stream().mapToDouble(Double::doubleValue).toArray();
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] >= prices[i - 1]) return false;
        }

        return true;
    }

    public double calculateAverageProfit() {
        if (transactions.isEmpty()) {
            return 0.0;
        }

        return calculateProfit() / transactions.size();
    }

    public double calculateProfit() {
        double initialBalance = 1000.0;
        double currentBalance = usdtBalance + btcBalance * (transactions.isEmpty() ? 0 : transactions.get(transactions.size() - 1).price());

        return currentBalance - initialBalance;
    }

    public int countSuccessfulTrades() {
        int successfulTrades = 0;
        for (int i = 0; i < transactions.size(); i++) {
            Transaction current = transactions.get(i);
            if (current.type().equals(OrderType.SELL)) {
                for (int j = 0; j < i; j++) {
                    Transaction previous = transactions.get(j);
                    if (previous.type().equals(OrderType.BUY) && previous.price() < current.price()) {
                        successfulTrades++;
                        break;
                    }
                }
            }
        }

        return successfulTrades;
    }
}
