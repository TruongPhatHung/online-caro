package com.carogame.backend.controller;

import com.carogame.backend.dto.LeaveMessage;
import com.carogame.backend.dto.MoveMessage;
import com.carogame.backend.model.Room;
import com.carogame.backend.service.GameHistoryService;
import com.carogame.backend.service.GameLogicService;
import com.carogame.backend.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private GameLogicService gameLogicService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameHistoryService gameHistoryService;

    // HAM MỚI: Dang ky WebSocket Session attributes khi vua vao phong
    @MessageMapping("/game.register")
    public void registerSession(@Payload LeaveMessage message, SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getPlayerName());
            headerAccessor.getSessionAttributes().put("roomId", message.getRoomId());
        }
    }

    @MessageMapping("/game.move")
    public void makeMove(@Payload MoveMessage move, SimpMessageHeaderAccessor headerAccessor) {
        // Luu lai session attribute phong truong hop chua register
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", move.getPlayerName());
            headerAccessor.getSessionAttributes().put("roomId", move.getRoomId());
        }

        Room room = roomService.getRoom(move.getRoomId());
        if (room == null || room.isGameOver()) return;

        // KIEM TRA DU SO LUONG NGUOI CHOI
        int currentPlayerCount = 0;
        if (room.getPlayerX() != null) currentPlayerCount++;
        if (room.getPlayerO() != null) currentPlayerCount++;
        if (room.getMaxPlayers() == 3 && room.getPlayerY() != null) {
            currentPlayerCount++;
        }

        if (currentPlayerCount < room.getMaxPlayers()) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + move.getRoomId() + "/system",
                    "Chưa đủ " + room.getMaxPlayers() + " người chơi. Ván đấu chưa thể bắt đầu!"
            );
            return;
        }

        String[][] board = room.getBoard();
        int r = move.getRow();
        int c = move.getCol();

        if (board[r][c] == null) {
            String symbol = "Y";
            if (move.getPlayerName().equals(room.getPlayerX())) symbol = "X";
            else if (move.getPlayerName().equals(room.getPlayerO())) symbol = "O";

            board[r][c] = symbol;
            int[][] winningLine = gameLogicService.checkWin(board, r, c, symbol);

            if (winningLine != null) {
                // 1. CÓ NGƯỜI THẮNG
                room.setGameOver(true);
                room.setWinner(symbol);
                room.setWinningLine(winningLine);
                gameHistoryService.saveGameResult(room.getRoomId(), room.getPlayerX(), room.getPlayerO(), symbol);
            }
            else if (gameLogicService.isBoardFull(board)) {
                // 2. BỔ SUNG LOGIC HÒA Ở ĐÂY
                room.setGameOver(true);
                room.setWinner("DRAW"); // Trả về "DRAW" để ReactJS hiển thị UI Hòa
                // Lưu vào database lịch sử trận đấu là hòa (tuỳ chọn)
                gameHistoryService.saveGameResult(room.getRoomId(), room.getPlayerX(), room.getPlayerO(), "DRAW");
            }
            else {
                // 3. CHƯA KẾT THÚC -> CHUYỂN LƯỢT
                if (room.getMaxPlayers() == 3) {
                    if (symbol.equals("X")) room.setCurrentTurn("O");
                    else if (symbol.equals("O")) room.setCurrentTurn("Y");
                    else room.setCurrentTurn("X");
                } else {
                    room.setCurrentTurn(symbol.equals("X") ? "O" : "X");
                }
            }
            messagingTemplate.convertAndSend("/topic/room/" + move.getRoomId(), room);
        }
    }

    @MessageMapping("/game.reset")
    public void resetGame(@Payload String roomId) {
        Room room = roomService.getRoom(roomId);
        if (room != null) {
            room.setBoard(new String[room.getBoardSize()][room.getBoardSize()]);
            room.setGameOver(false);
            room.setWinner(null);
            room.setWinningLine(null);
            room.setCurrentTurn("X");
            messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
        }
    }

    @MessageMapping("/game.timeout")
    public void handleTimeout(@Payload String roomId) {
        Room room = roomService.getRoom(roomId);
        if (room != null && !room.isGameOver()) {
            room.setGameOver(true);

            String winner = "X";
            if (room.getCurrentTurn().equals("X")) winner = "O";
            else if (room.getCurrentTurn().equals("O")) winner = (room.getMaxPlayers() == 3) ? "Y" : "X";

            room.setWinner(winner);
            gameHistoryService.saveGameResult(room.getRoomId(), room.getPlayerX(), room.getPlayerO(), winner);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
        }
    }

    @MessageMapping("/game.leave")
    public void handleLeave(@Payload LeaveMessage leave) {
        Room room = roomService.getRoom(leave.getRoomId());
        if (room == null) return;

        String playerName = leave.getPlayerName();
        boolean isX = playerName.equals(room.getPlayerX());
        boolean isO = playerName.equals(room.getPlayerO());
        boolean isY = playerName.equals(room.getPlayerY());

        if (isX) room.setPlayerX(null);
        else if (isO) room.setPlayerO(null);
        else if (isY) room.setPlayerY(null);

        // BAT KY AI THOAT CUING HUY TRAN
        room.setGameOver(true);
        room.setWinner("CANCELLED");

        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), room);
    }
}