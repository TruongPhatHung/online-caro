package com.carogame.backend.controller;

import com.carogame.backend.model.Room;
import com.carogame.backend.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 1. Bổ sung Import này
import org.springframework.web.bind.annotation.*;
import com.carogame.backend.dto.CreateRoomRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // 2. Inject SimpMessagingTemplate

    // API Tạo phòng
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            Room room = roomService.createRoom(
                    request.getPlayerName(),
                    request.getMaxPlayers(),
                    request.getBoardSize()
            );
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // API Tham gia phòng
    @PostMapping("/join")
    public ResponseEntity<Room> joinRoom(@RequestBody Map<String, String> payload) {
        String roomId = payload.get("roomId");
        String playerName = payload.get("playerName");

        Room room = roomService.joinRoom(roomId, playerName);
        if (room != null) {
            // 3. THÊM DÒNG NÀY: Bắn thông tin phòng cập nhật tới tất cả người chơi qua WebSocket
            messagingTemplate.convertAndSend("/topic/room/" + roomId, room);

            return ResponseEntity.ok(room); // Vào phòng thành công
        }
        return ResponseEntity.badRequest().body(null); // Lỗi mã phòng sai hoặc phòng đã đủ người
    }
}