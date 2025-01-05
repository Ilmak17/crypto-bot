package com.trading.bot.bots;

import com.trading.bot.model.OrderType;
import com.trading.bot.model.Transaction;

import java.util.List;
import java.util.Queue;

public interface Bot {
    void performAction(double price);

    void getBalance();

    List<Transaction> getTransactionHistory();

    Double getUsdtBalance();

    Double getBtcBalance();

    default boolean isTrendingUp(Queue<Double> priceHistory) {
        if (priceHistory.size() < 2) return false;

        double[] prices = priceHistory.stream().mapToDouble(Double::doubleValue).toArray();
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] <= prices[i - 1]) return false;
        }

        return true;
    }

    default int countSuccessfulTrades() {
        int successfulTrades = 0;
        List<Transaction> transactions = getTransactionHistory();
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

    default double calculateAverageProfit() {
        if (getTransactionHistory().isEmpty()) {
            return 0.0;
        }
        return calculateProfit() / getTransactionHistory().size();
    }

    default double calculateProfit() {
        List<Transaction> transactionHistory = getTransactionHistory();
        Double btcBalance = getBtcBalance();
        Double usdtBalance = getUsdtBalance();

        double initialBalance = 1000.0;
        double currentBalance = usdtBalance + btcBalance
                * (transactionHistory.isEmpty() ? 0 : transactionHistory.get(transactionHistory.size() - 1).price());

        return currentBalance - initialBalance;
    }

}
