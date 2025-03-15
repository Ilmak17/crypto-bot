package com.trading.bot.api;

import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.api.mapper.OrderBookDtoMapper;
import com.trading.bot.api.mapper.PriceDtoMapper;
import com.trading.bot.api.util.BinanceSignature;
import com.trading.bot.model.enums.Symbol;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final String API_KEY = "";
    private static final String API_SECRET = "";

    private final SpotClient client;

    public BinanceApiClientBean() {
        this.client = new SpotClientImpl(API_KEY, SECRET_KEY);
    }
}
