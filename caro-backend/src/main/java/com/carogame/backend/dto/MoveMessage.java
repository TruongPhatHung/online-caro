package com.carogame.backend.dto;

public class MoveMessage {
    private String roomId;
    private String playerName;
    private int row;
    private int col;

    // Các hàm Getter và Setter bắt buộc phải có để Spring Boot tự động map dữ liệu
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
}