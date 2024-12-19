package com.trading.bot.bots;

import java.util.Random;

public class DumbBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final Random random;

    public DumbBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        random = new Random();
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

    private void buy(double price) {
        double spendAmount = random.nextDouble(usdtBalance);
        if (spendAmount > 0) {
            double btcBought = spendAmount / price;
            usdtBalance -= spendAmount;
            btcBalance += btcBought;

            System.out.printf("%s bought %.6f BTC for %.2f USDT.%n", name, btcBought, spendAmount);
        }
    }

    private void sell(double price) {
        double btcToSell = random.nextDouble(btcBalance);
        if (btcToSell > 0) {
            double earnedUsdt = btcToSell * price;
            btcBalance -= btcToSell;
            usdtBalance += earnedUsdt;

            System.out.printf("%s sold %.6f BTC for %.2f USDT.%n", name, btcToSell, earnedUsdt);
        }
    }
}