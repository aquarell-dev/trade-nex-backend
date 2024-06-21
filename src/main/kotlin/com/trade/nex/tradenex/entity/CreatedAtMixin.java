package com.trade.nex.tradenex.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class CreatedAtMixin {
    private LocalDateTime createdAt;
}