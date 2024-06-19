package com.trade.nex.tradenex.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class KuCoin extends Exchange {
    private static final String BASE_URL = "https://api.kucoin.com";
    private static final Logger logger = Logger.getLogger(KuCoin.class.getName());
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public KuCoin() {
        super("KuCoin");
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getDepositUrl(String coin) {
        return "https://www.kucoin.com/assets/coin/" + coin.toUpperCase();
    }

    @Override
    public String getWithdrawUrl(String coin) {
        return "https://www.kucoin.com/trade/" + coin.toUpperCase() + "-USDT";
    }

    @Override
    public String getTradeUrl(String coin) {
        return "https://www.kucoin.com/assets/withdraw/" + coin.toUpperCase();
    }

    @Override
    public String getCoinMarketCapName() {
        return "KuCoin";
    }

    @Override
    public String getCoinMarketCapSlug() {
        return "kucoin";
    }

    @Override
    public CompletableFuture<Map<String, Object>> getNetworks() {
        return fetchCurrencies().thenApply(currencies -> {
            Map<String, Object> formattedCurrencies = new HashMap<>();

            currencies.forEach((coin, coinData) -> {
                JsonNode networks = coinData.get("networks");
                ((ObjectNode) coinData).remove("networks");

                Map<String, Object> formattedNetworks = new HashMap<>();
                networks.fields().forEachRemaining(entry -> {
                    JsonNode networkData = entry.getValue();
                    Map<String, Object> networkDetails = new HashMap<>();
                    networkDetails.putAll(objectMapper.convertValue(networkData, Map.class));
                    networkDetails.put("network", networkData.get("code").asText());
                    networkDetails.put("is_percentage", !networkData.get("info").get("withdrawFeeRate").asText().equals("0"));
                    formattedNetworks.put(entry.getKey(), networkDetails);
                });

                ((ObjectNode) coinData).put("networks", objectMapper.valueToTree(formattedNetworks));
                formattedCurrencies.put(coin, objectMapper.convertValue(coinData, Map.class));
            });

            return formattedCurrencies;
        });
    }

    @Override
    public CompletableFuture<Map<String, JsonNode>> fetchCurrencies() {
        String url = BASE_URL + "/api/v1/currencies";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("KuCoin response status is not 200, but " + response.statusCode());
                        throw new RuntimeException("Failed to fetch currencies");
                    }

                    try {
                        JsonNode body = objectMapper.readTree(response.body());
                        JsonNode data = body.get("data");
                        Map<String, JsonNode> currencies = new HashMap<>();
                        data.fields().forEachRemaining(entry -> currencies.put(entry.getKey(), entry.getValue()));
                        return currencies;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse response body", e);
                    }
                });
    }
}
