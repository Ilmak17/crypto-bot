package com.trading.bot.dao;

import com.trading.bot.bots.Bot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDaoBean implements BotDao {
    private final Map<Long, Bot> bots = new HashMap<>();

    @Override
    public void add(Long id, Bot bot) {
        bots.put(id, bot);
        System.out.println("Added bot with ID: " + id);
    }

    @Override
    public Bot get(Long id) {
        return bots.get(id);
    }

    @Override
    public void remove(Long id) {
        bots.remove(id);
        System.out.println("Removed bot with ID: " + id);
    }

    @Override
    public List<Bot> getAll() {
        return new ArrayList<>(bots.values());
    }
}
