package com.trade.nex.tradenex.exchange;

import java.util.List;
import java.util.Map;

public class MarketData {
    private final Exchange exchange;
    private final int numMarkets;
    private final List<Map<String, Object>> markets;

    public MarketData(Exchange exchange, int numMarkets, List<Map<String, Object>> markets) {
        this.exchange = exchange;
        this.numMarkets = numMarkets;
        this.markets = markets;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public int getNumMarkets() {
        return numMarkets;
    }

    public List<Map<String, Object>> getMarkets() {
        return markets;
    }
}
