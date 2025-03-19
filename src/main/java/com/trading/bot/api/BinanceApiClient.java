package com.trading.bot.api;

import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.model.enums.Symbol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

public interface BinanceApiClient {

    Double getPrice(Symbol symbol);

    OrderBookDto getOrderBook(Symbol market, int limit);

    BiFunction<Symbol, Integer, Map<String, Object>> toRequest = (symbol, limit) -> {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", symbol.toString());

        if (nonNull(limit)) {
            params.put("limit", limit);
        }

        return params;
    };
}
