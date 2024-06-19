package com.trade.nex.tradenex.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public abstract class Exchange {
    private static final Logger logger = Logger.getLogger(Exchange.class.getName());
    private String name;

    public Exchange(String name) {
        this.name = name;
    }

    public abstract String getDepositUrl(String coin);
    public abstract String getWithdrawUrl(String coin);
    public abstract String getTradeUrl(String coin);
    public abstract String getCoinMarketCapName();
    public abstract String getCoinMarketCapSlug();

    public CompletableFuture<Map<String, Object>> getNetworks() {
        return fetchCurrencies().thenApply(currs -> {
            Map<String, Object> networks = new HashMap<>();
            // Process currencies and extract networks data
            // This part should be implemented based on your specific requirements
            return networks;
        });
    }

    public CompletableFuture<Map<String, JsonNode>> fetchCurrencies() {
        // This method should be implemented to fetch currencies from the exchange
        // For demonstration purposes, we'll just return a completed future
        return CompletableFuture.completedFuture(new HashMap<>());
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Map<String, Object> loadConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> configuration = new HashMap<>();

        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("exchanges/exchanges.conf.json"));
            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode configNode = rootNode.get(this.name.toUpperCase());
            configuration = objectMapper.convertValue(configNode, Map.class);
        } catch (IOException e) {
            logger.severe("Failed to load configuration: " + e.getMessage());
        }

        return configuration;
    }
}
