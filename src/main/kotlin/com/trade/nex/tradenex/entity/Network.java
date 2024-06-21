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
@Table(name = "Network")
public class Network {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false)
    private String chain;

    // Assuming Coin is another entity related to Network
    // Add appropriate JPA annotations for the relationship
    // For simplicity, assuming One-to-Many relationship
    // Replace "Coin" with the actual entity class name if different
    @OneToMany(mappedBy = "network", cascade = CascadeType.ALL)
    private List<Coin> coins;
}