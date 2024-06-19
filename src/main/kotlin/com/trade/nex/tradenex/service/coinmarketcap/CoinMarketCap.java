package com.trade.nex.tradenex.service.coinmarketcap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trade.nex.tradenex.exchange.Exchange;
import com.trade.nex.tradenex.exchange.MarketData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.get;

public class CoinMarketCap {
    private static final String API_BASE_URL = "https://api.coinmarketcap.com";
    private static final int MAX_MARKET_CAP_LIMIT = 1000;
    private static final Logger logger = Logger.getLogger(CoinMarketCap.class.getName());

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> defaultHeaders;

    public CoinMarketCap() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
        this.defaultHeaders = new HashMap<>() {{
            put("Accept", "application/json");
            put("Content-Type", "application/json");
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/89.0.4389.105 "
                    + "YaBrowser/21.3.3.230 Yowser/2.5 Safari/537.36");
        }};
    }

    public CompletableFuture<MarketData> getMarkets(
            Exchange exchange,
            String proxy,
            int start,
            int limit,
            int delay) {

        String url = getMarketsRelativeUrl(exchange.getCoinMarketCapSlug(), start, limit);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + url))
                .timeout(Duration.ofMinutes(1));

        defaultHeaders.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("CoinMarketCap response status is not 200, but " + response.statusCode());
                    }

                    JsonNode body;
                    try {
                        body = objectMapper.readTree(response.body());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse response body", e);
                    }

                    JsonNode status = body.get("status");
                    int errorCode = status.get("error_code").asInt();

                    if (errorCode != 0) {
                        logger.severe("CoinMarketCap error(" + errorCode + ") - " + status.get("error_message").asText());
                    }

                    JsonNode data = body.get("data");
                    int numMarkets = data.get("numMarketPairs").asInt();

                    List<Map<String, Object>> markets = new ArrayList<>();
                    for (JsonNode market : data.get("marketPairs")) {
                        String symbol = market.get("baseSymbol").asText();
                        Map<String, Object> marketData = new HashMap<>();
                        marketData.put("exchange", exchange);
                        marketData.put("coin", symbol);
                        marketData.put("currency_name", market.get("baseCurrencyName").asText());
                        marketData.put("volume", market.get("volumeUsd").asDouble(0));
                        marketData.put("trade_url", exchange.getTradeUrl(symbol));
                        marketData.put("withdraw_url", exchange.getWithdrawUrl(symbol));
                        marketData.put("deposit_url", exchange.getDepositUrl(symbol));
                        markets.add(marketData);
                    }

                    try {
                        TimeUnit.SECONDS.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return new MarketData(exchange, numMarkets, markets);
                });
    }

    private String getMarketsRelativeUrl(String exchangeSlug, int start, int limit) {
        return "/data-api/v3/exchange/market-pairs/latest?slug=" + exchangeSlug
                + "&category=spot&start=" + start + "&limit=" + limit + "&quoteCurrencyId=825";
    }
}
