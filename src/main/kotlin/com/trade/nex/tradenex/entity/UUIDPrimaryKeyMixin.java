package com.trade.nex.tradenex.entity;

import lombok.Data;

import java.util.UUID;

@Data
public abstract class UUIDPrimaryKeyMixin {
    private UUID id;
}
