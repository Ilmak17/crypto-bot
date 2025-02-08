package com.trading.bot.dao;

import com.trading.bot.bots.Bot;

import java.util.List;

public interface BotDao {
    void add(Long id, Bot bot);

    Bot get(Long id);

    void remove(Long id);

    List<Bot> getAll();
}
