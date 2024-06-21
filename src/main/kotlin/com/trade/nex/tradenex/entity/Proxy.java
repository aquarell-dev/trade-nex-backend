package com.trade.nex.tradenex.entity;

import com.trade.nex.tradenex.enums.ProxyType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Proxy")
public class Proxy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 10) // Adjust length as needed
    private ProxyType type;

    @Column(length = 64)
    private String host;

    @Column(length = 6)
    private String port;

    @Column(length = 264)
    private String username;

    @Column(length = 264)
    private String password;

    @Column(name = "active_until")
    private LocalDateTime activeUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
