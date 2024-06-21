package com.trade.nex.tradenex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "CoinsNetwork")
public class CoinNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", nullable = false)
    private Network network;

    @Column(nullable = false)
    private Double fee;

    @Column(nullable = false)
    private Boolean withdrawable;

    @Column(nullable = false)
    private Boolean depositable;
}
