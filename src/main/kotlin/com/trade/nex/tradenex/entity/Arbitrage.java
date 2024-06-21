package com.trade.nex.tradenex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "Arbitrage")
public class Arbitrage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_id", nullable = false)
    private Coin buy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_id", nullable = false)
    private Coin sell;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", nullable = false)
    private Network network;

    @ManyToMany
    @JoinTable(
            name = "arbitrages_offers",
            joinColumns = @JoinColumn(name = "arbitrage_id"),
            inverseJoinColumns = @JoinColumn(name = "offer_id")
    )
    private Set<Offer> offers;
}
