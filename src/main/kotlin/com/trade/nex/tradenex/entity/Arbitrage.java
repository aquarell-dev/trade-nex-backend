package com.trade.nex.tradenex.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Arbitrage {
    @Id
    private UUID id;
}
