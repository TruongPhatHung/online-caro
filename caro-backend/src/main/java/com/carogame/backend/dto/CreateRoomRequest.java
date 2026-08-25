package com.carogame.backend.dto;

import lombok.Data;

@Data
public class CreateRoomRequest {
    private String playerName;
    private int maxPlayers = 2; // Mặc định 2 người
    private int boardSize = 15; // Mặc định 15x15
}