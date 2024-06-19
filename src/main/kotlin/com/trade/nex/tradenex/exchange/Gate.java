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

public class Gate extends Exchange {
    private static final Logger logger = Logger.getLogger(Gate.class.getName());
    private static final String API_KEY = "caf0adde67cf643ab18002a1a5d90a92";
    private static final String API_SECRET = "2eba4e101152767fb26dac66980ade362bcbb327f4cc185ba1abbd92038c6f57";
    private static final String BASE_API_URL = "https://api.gateio.ws";
    private static final String BASE_URL = "https://www.gate.io";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Gate() {
        super("Gate.io");
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getDepositUrl(String coin) {
        return BASE_URL + "/myaccount/deposit/" + coin.toUpperCase();
    }

    @Override
    public String getWithdrawUrl(String coin) {
        return BASE_URL + "/myaccount/withdraw/" + coin.toUpperCase();
    }

    @Override
    public String getTradeUrl(String coin) {
        return BASE_URL + "/trade/" + coin.toUpperCase() + "_USDT";
    }

    @Override
    public String getCoinMarketCapName() {
        return "Gate.io";
    }

    @Override
    public String getCoinMarketCapSlug() {
        return "gate-io";
    }

    @Override
    public CompletableFuture<Map<String, Object>> getNetworks() {
        CompletableFuture<Map<String, JsonNode>> fetchCurrenciesFuture = fetchCurrencies();
        CompletableFuture<Map<String, JsonNode>> fetchFeesFuture = fetchDepositWithdrawFees();

        return CompletableFuture.allOf(fetchCurrenciesFuture, fetchFeesFuture)
                .thenApply(v -> {
                    Map<String, JsonNode> fetchedCurrencies = fetchCurrenciesFuture.join();
                    Map<String, JsonNode> fees = fetchFeesFuture.join();

                    Map<String, Object> currencies = new HashMap<>();

                    fees.forEach((symbol, feeInfo) -> {
                        JsonNode networks = fetchedCurrencies.get(symbol).get("networks");
                        JsonNode networkFees = feeInfo.get("networks");

                        Map<String, Object> formattedNetworks = new HashMap<>();
                        networks.fields().forEachRemaining(entry -> {
                            String networkName = entry.getKey();
                            JsonNode network = entry.getValue();
                            String networkId = network.get("id").asText();
                            JsonNode feeInfoNode = networkFees.get(networkId);
                            if (feeInfoNode != null) {
                                Map<String, Object> networkDetails = new HashMap<>();
                                networkDetails.put("network", network.get("network").asText());
                                networkDetails.put("fee", feeInfoNode.get("withdraw").get("fee").asText());
                                networkDetails.put("deposit", network.get("deposit").asBoolean());
                                networkDetails.put("withdraw", network.get("withdraw").asBoolean());
                                boolean isPercentage = !"0%".equals(feeInfo.get("info").get("withdraw_percent_on_chains").get(networkId).asText());
                                networkDetails.put("is_percentage", isPercentage);
                                formattedNetworks.put(networkName, networkDetails);
                            }
                        });

                        Map<String, Object> currencyDetails = new HashMap<>();
                        currencyDetails.put("networks", formattedNetworks);
                        currencies.put(symbol, currencyDetails);
                    });

                    return currencies;
                });
    }

    public CompletableFuture<Map<String, JsonNode>> fetchCurrencies() {
        String url = BASE_API_URL + "/api/v4/currencies";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("KEY", API_KEY)
                .header("SECRET", API_SECRET)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("Gate.io response status is not 200, but " + response.statusCode());
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

    private CompletableFuture<Map<String, JsonNode>> fetchDepositWithdrawFees() {
        String url = BASE_API_URL + "/api/v4/withdraw_fees";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("KEY", API_KEY)
                .header("SECRET", API_SECRET)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.severe("Gate.io response status is not 200, but " + response.statusCode());
                        throw new RuntimeException("Failed to fetch deposit/withdraw fees");
                    }

                    try {
                        JsonNode body = objectMapper.readTree(response.body());
                        Map<String, JsonNode> fees = new HashMap<>();
                        body.fields().forEachRemaining(entry -> fees.put(entry.getKey(), entry.getValue()));
                        return fees;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse response body", e);
                    }
                });
    }
}
