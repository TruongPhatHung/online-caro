package com.carogame.backend.dto;

public class LeaveMessage {
    private String roomId;
    private String playerName;

    // Getters và Setters bắt buộc phải có
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}