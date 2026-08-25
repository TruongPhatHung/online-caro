package com.carogame.backend.service;

import com.carogame.backend.model.Room;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {
    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Cập nhật hàm tạo phòng
    public Room createRoom(String playerName, int maxPlayers, int boardSize) {
        // Tạo mã phòng 6 ký tự
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Room room = new Room(roomId, maxPlayers, boardSize);
        room.setPlayerX(playerName);

        rooms.put(roomId, room);
        return room;
    }

    // Cập nhật hàm tham gia phòng
    public Room joinRoom(String roomId, String playerName) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Phòng không tồn tại!");
        }

        // Nếu đã ở trong phòng rồi thì bỏ qua
        if (playerName.equals(room.getPlayerX()) ||
                playerName.equals(room.getPlayerO()) ||
                playerName.equals(room.getPlayerY())) {
            return room;
        }

        // Xếp ghế cho người mới vào
        if (room.getPlayerO() == null) {
            room.setPlayerO(playerName);
        } else if (room.getMaxPlayers() == 3 && room.getPlayerY() == null) {
            room.setPlayerY(playerName); // Cho người thứ 3 vào
        } else {
            throw new RuntimeException("Phòng đã đầy!");
        }

        return room;
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room leaveRoom(String roomId, String playerName) {
        Room room = rooms.get(roomId);
        if (room != null && playerName != null) {

            // 1. Xác định ai vừa thoát và set thành null
            if (playerName.equals(room.getPlayerX())) {
                room.setPlayerX(null);
            } else if (playerName.equals(room.getPlayerO())) {
                room.setPlayerO(null);
            } else if (playerName.equals(room.getPlayerY())) {
                room.setPlayerY(null);
            }

            // 2. Đếm số người chơi còn lại
            int activePlayers = 0;
            if (room.getPlayerX() != null) activePlayers++;
            if (room.getPlayerO() != null) activePlayers++;
            if (room.getPlayerY() != null) activePlayers++;

            // 3. Nếu phòng trống không còn ai -> Xóa luôn phòng cho nhẹ bộ nhớ
            if (activePlayers == 0) {
                rooms.remove(roomId);
                return null; // Trả về null vì phòng không còn tồn tại
            }

            // 4. Nếu số người chơi còn lại ít hơn 2 (Chỉ còn 1 người) -> Bắt buộc kết thúc
            if (activePlayers < 2) {
                room.setGameOver(true);
                room.setWinner("CANCELLED"); // Bắn cờ này để Frontend biết mà "đá" người còn lại về sảnh
                rooms.remove(roomId); // Xóa luôn phòng trên Server để dọn dẹp bộ nhớ
            }

            return room; // Trả về object room để Controller broadcast thông điệp cuối cùng
        }
        return null;
    }
}