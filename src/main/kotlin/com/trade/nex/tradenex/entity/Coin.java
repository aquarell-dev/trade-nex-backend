package com.trade.nex.tradenex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "Coin")
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32, nullable = false)
    private String coin;

    @Column(length = 128, nullable = false)
    private String currencyName;

    @Column(length = 32, nullable = false)
    private String exchange;

    @Column
    private Double volume;

    @Column(length = 256, nullable = false)
    private String withdrawUrl;

    @Column(length = 256, nullable = false)
    private String depositUrl;

    @Column(length = 256, nullable = false)
    private String tradeUrl;

    // Assuming Network is another entity related to Coin
    // Add appropriate JPA annotations for the relationship
    // For simplicity, assuming One-to-Many relationship
    // Replace "Network" with the actual entity class name if different
    @OneToMany(mappedBy = "coin", cascade = CascadeType.ALL)
    private List<Network> networks;
}
