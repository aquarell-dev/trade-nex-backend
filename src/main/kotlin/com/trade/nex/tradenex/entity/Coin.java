package com.trade.nex.tradenex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
public class Coin
{
    @Id
    private UUID id;
    private String name;
    @Column(name = "currecy_name")
    private String currencyName;
    private String exchange;
    private Float volume;
    @Column(name = "trade_url")
    private String tradeUrl;
}