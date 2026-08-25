package com.carogame.backend.model;

import lombok.Data;

@Data
public class Room {
    private String roomId;
    private String playerX;
    private String playerO;
    private String playerY; // Dành cho chế độ 3 người chơi

    private int maxPlayers;
    private int boardSize;

    private String[][] board;
    private String currentTurn;
    private boolean isGameOver;
    private String winner;
    private int[][] winningLine;

    // Constructor linh hoạt
    public Room(String roomId, int maxPlayers, int boardSize) {
        this.roomId = roomId;
        this.maxPlayers = maxPlayers;
        this.boardSize = boardSize;
        this.board = new String[boardSize][boardSize]; // Khởi tạo bàn cờ động
        this.currentTurn = "X";
        this.isGameOver = false;
    }
}