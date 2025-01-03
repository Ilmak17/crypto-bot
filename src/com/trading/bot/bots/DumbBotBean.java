package com.trading.bot.bots;

import com.trading.bot.model.OrderType;
import com.trading.bot.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DumbBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final Random random;
    private final List<Transaction> transactions;

    public DumbBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        random = new Random();
        transactions = new ArrayList<>();
    }

    @Override
    public void performAction(double price) {
        switch (random.nextInt(3)) {
            case 0 -> System.out.println(name + " decided to skip.");
            case 1 -> buy(price);
            case 2 -> sell(price);
            default -> System.out.println("Error has occurred");
        }
    }

    @Override
    public void getBalance() {
        System.out.printf("%s Balance: %.2f USDT | %.6f BTC%n", name, usdtBalance, btcBalance);
    }

    @Override
    public void getTransactionHistory() {
        System.out.println(name + " History");
        transactions.forEach(System.out::println);
    }

    private void buy(double price) {
        double spendAmount = random.nextDouble(usdtBalance);
        if (spendAmount > 0) {
            double btcBought = spendAmount / price;
            usdtBalance -= spendAmount;
            btcBalance += btcBought;

            transactions.add(new Transaction(OrderType.BUY, price, btcBought, spendAmount));
            System.out.printf("%s bought %.6f BTC for %.2f USDT.%n", name, btcBought, spendAmount);
        }
    }

    private void sell(double price) {
        double btcToSell = random.nextDouble(btcBalance);
        if (btcToSell > 0) {
            double earnedUsdt = btcToSell * price;
            btcBalance -= btcToSell;
            usdtBalance += earnedUsdt;

            transactions.add(new Transaction(OrderType.SELL, price, btcToSell, earnedUsdt));
            System.out.printf("%s sold %.6f BTC for %.2f USDT.%n", name, btcToSell, earnedUsdt);
        }
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
