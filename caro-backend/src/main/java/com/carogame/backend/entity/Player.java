package com.carogame.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Player {
    @Id
    private String playerName; // Dùng tên làm khóa chính

    private int wins = 0;
    private int losses = 0;
}