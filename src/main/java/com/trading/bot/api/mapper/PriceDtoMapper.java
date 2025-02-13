package com.trading.bot.api.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.bot.api.dto.PriceDto;
import lombok.SneakyThrows;

public class PriceDtoMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public PriceDto toPriceDto(String json) {
        return objectMapper.readValue(json, PriceDto.class);
    }
}
