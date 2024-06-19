package com.trade.nex.tradenex.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class Bitget extends Exchange {
    private static final Logger logger = Logger.getLogger(Bitget.class.getName());
    private static final String BASE_API_URL = "https://api.bitget.com";
    private static final String BASE_URL = "https://www.bitget.com";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Bitget() {
        super("Bitget");
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getDepositUrl(String coin) {
        return BASE_URL + "/asset/recharge";
    }

    @Override
    public String getWithdrawUrl(String coin) {
        return BASE_URL + "/asset/withdraw";
    }

    @Override
    public String getTradeUrl(String coin) {
        return BASE_URL + "/spot/" + coin.toUpperCase() + "USDT";
    }

    @Override
    public String getCoinMarketCapName() {
        return "Bitget";
    }

    @Override
    public String getCoinMarketCapSlug() {
        return "bitget";
    }

    @Override
    public CompletableFuture<Map<String, Object>> getNetworks() {
        return fetchCurrencies().thenApply(currencies -> {
            Map<String, Object> formattedCurrencies = new HashMap<>();

            currencies.forEach((currency, currencyInfo) -> {
                JsonNode networks = currencyInfo.get("networks");
                Map<String, Object> formattedNetworks = new HashMap<>();

                networks.fields().forEachRemaining(entry -> {
                    String code = entry.getKey();
                    JsonNode network = entry.getValue();
                    boolean isPercentage = !"0".equals(network.get("info").get("extraWithdrawFee").asText());

                    Map<String, Object> networkDetails = objectMapper.convertValue(network, Map.class);
                    networkDetails.put("is_percentage", isPercentage);

                    formattedNetworks.put(code, networkDetails);
                });

                Map<String, Object> currencyDetails = objectMapper.convertValue(currencyInfo, Map.class);
                currencyDetails.put("networks", formattedNetworks);

                formattedCurrencies.put(currency, currencyDetails);
            });

            return formattedCurrencies;
        });
    }

    public CompletableFuture<Map<String, JsonNode>> fetchCurrencies() {
        String url = BASE_API_URL + "/api/currency/getCurrencies";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("Bitget response status is not 200, but " + response.statusCode());
                        throw new RuntimeException("Failed to fetch currencies");
                    }

                    try {
                        JsonNode body = objectMapper.readTree(response.body());
                        Map<String, JsonNode> currencies = new HashMap<>();
                        body.fields().forEachRemaining(entry -> currencies.put(entry.getKey(), entry.getValue()));
                        return currencies;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse response body", e);
                    }
                });
    }
}
