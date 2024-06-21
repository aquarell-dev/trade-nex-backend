package com.trade.nex.tradenex.enums;

public enum Exchanges {
    MEXC("MEXC"),
    GATE("Gate.io"),
    BITGET("Bitget"),
    HTX("HTX"),
    BITMART("BitMart"),
    KUCOIN("KuCoin"),
    BYBIT("Bybit"),
    POLONIEX("Poloniex"),
    BINGX("BingX");

    private final String name;

    Exchanges(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}