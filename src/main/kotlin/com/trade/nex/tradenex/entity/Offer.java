package com.trade.nex.tradenex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "Offer")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int volume;

    @Column(nullable = false, precision = 10, scale = 2)
    private double spread;

    @Column(nullable = false, precision = 10, scale = 2)
    private double profit;

    @Column(nullable = false)
    private double buy;

    @Column(nullable = false)
    private double sell;

    @Column(nullable = false)
    private double transactionFee;
}
