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

public class Mexc extends Exchange{
    private static final Logger logger = Logger.getLogger(Mexc.class.getName());
    private static final String API_KEY = "mx0vgl5HkKTBuco0uc";
    private static final String API_SECRET = "57c19efb62d944a7b6e48951e8caec43";
    private static final String BASE_URL = "https://api.mexc.com/api/";
    private static final String RECV_WINDOW = "5000";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Mexc() {
        super("MEXC");
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getDepositUrl(String coin) {
        return "https://www.mexc.com/ru-RU/assets/deposit/" + coin.toUpperCase();
    }

    @Override
    public String getWithdrawUrl(String coin) {
        return "https://www.mexc.com/ru-RU/assets/withdraw/" + coin.toUpperCase();
    }

    @Override
    public String getTradeUrl(String coin) {
        return "https://www.mexc.com/ru-RU/exchange/" + coin.toUpperCase() + "_USDT";
    }

    @Override
    public String getCoinMarketCapName() {
        return "MEXC";
    }

    @Override
    public String getCoinMarketCapSlug() {
        return "mexc";
    }

    @Override
    public CompletableFuture<Map<String, Object>> getNetworks() {
        return fetchCurrencies().thenApply(currencies -> {
            Map<String, Object> formattedCurrencies = new HashMap<>();

            currencies.forEach((currency, currencyInfo) -> {
                JsonNode currencyNetworks = currencyInfo.get("networks");
                ((ObjectNode) currencyInfo).remove("networks");

                Map<String, Object> formattedNetworks = new HashMap<>();
                currencyNetworks.fields().forEachRemaining(entry -> {
                    JsonNode network = entry.getValue();
                    Map<String, Object> networkDetails = new HashMap<>();
                    networkDetails.putAll(objectMapper.convertValue(network, Map.class));
                    String withdrawTips = network.get("info").get("withdrawTips").asText();
                    networkDetails.put("is_percentage", withdrawTips != null && (withdrawTips.contains("%") || withdrawTips.contains("fee")));
                    formattedNetworks.put(entry.getKey(), networkDetails);
                });

                ((ObjectNode) currencyInfo).put("networks", objectMapper.valueToTree(formattedNetworks));
                formattedCurrencies.put(currency, objectMapper.convertValue(currencyInfo, Map.class));
            });

            return formattedCurrencies;
        });
    }

    @Override
    public CompletableFuture<Map<String, JsonNode>> fetchCurrencies() {
        String url = BASE_URL + "v3/currencies";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("X-MXC-APIKEY", API_KEY)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("MEXC response status is not 200, but " + response.statusCode());
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
