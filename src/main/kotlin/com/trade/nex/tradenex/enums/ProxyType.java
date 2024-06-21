package com.trade.nex.tradenex.enums;

public enum ProxyType {
    HTTP("http"),
    HTTPS("https"),
    SOCKS5("socks5");

    private final String type;

    ProxyType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
