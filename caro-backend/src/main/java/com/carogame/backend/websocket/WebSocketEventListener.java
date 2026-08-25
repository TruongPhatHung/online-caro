package com.carogame.backend.websocket;

import com.carogame.backend.model.Room;
import com.carogame.backend.service.RoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        // Tránh NullPointerException nếu session attributes chưa được khởi tạo
        if (sessionAttributes == null) {
            return;
        }

        String username = (String) sessionAttributes.get("username");
        String roomId = (String) sessionAttributes.get("roomId");

        if (roomId != null) {
            Room room = roomService.getRoom(roomId);
            if (room != null && !room.isGameOver()) {
                if (username != null) {
                    if (username.equals(room.getPlayerX())) room.setPlayerX(null);
                    else if (username.equals(room.getPlayerO())) room.setPlayerO(null);
                    else if (username.equals(room.getPlayerY())) room.setPlayerY(null);
                }

                // Đánh dấu hủy trận đấu ngay khi có người rớt mạng / tắt tab
                room.setGameOver(true);
                room.setWinner("CANCELLED");

                // Broadcast trạng thái hủy phòng tới tất cả người chơi còn lại
                messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
            }
        }
    }
}