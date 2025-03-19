package com.trading.bot.api;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PriceDto;
import com.trading.bot.api.mapper.OrderBookDtoMapper;
import com.trading.bot.api.mapper.PriceDtoMapper;
import com.trading.bot.model.enums.Symbol;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BinanceApiClientBean.class);
    private static final SpotClient client;

    static {
        Dotenv dotenv = Dotenv.load();
        String baseUrl = dotenv.get("BINANCE_BASE_URL", "https://api.binance.com");
        String apiKey = dotenv.get("BINANCE_API_KEY");
        String secretKey = dotenv.get("BINANCE_SECRET_KEY");

        if (apiKey == null || secretKey == null) {
            throw new IllegalStateException("Missing Binance API credentials in .env file");
        }

        client = new SpotClientImpl(apiKey, secretKey, baseUrl);
        logger.info("Binance API Client initialized with base URL: {}", baseUrl);
    }

    @Override
    public Double getPrice(Symbol symbol) {
        try {
            String response = client.createMarket().tickerSymbol(toRequest.apply(symbol, null));

            PriceDto priceDto = new PriceDtoMapper().toPriceDto(response);
            logger.info("Fetched BTC price: {}", priceDto.getPrice());

            return priceDto.getPrice();
        } catch (Exception e) {
            logger.error("Failed to fetch {} price", symbol, e);
            return null;
        }
    }

    @Override
    public OrderBookDto getOrderBook(Symbol market, int limit) {
        try {
            String response = client.createMarket().depth(toRequest.apply(market, limit));

            OrderBookDto orderBook = new OrderBookDtoMapper().toOrderBookDto(response);
            logger.info("Fetched order book for {}: {}", market, orderBook);

            return orderBook;
        } catch (Exception e) {
            logger.error("Failed to fetch order book for {}", market, e);
            return null;
        }
    }

}
