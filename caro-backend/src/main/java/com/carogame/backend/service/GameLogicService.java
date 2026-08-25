package com.carogame.backend.service;

import org.springframework.stereotype.Service;

@Service
public class GameLogicService {

    public int[][] checkWin(String[][] board, int row, int col, String symbol) {
        int size = board.length; // Lấy kích thước thật của bàn cờ

        // 4 Hướng: Ngang, Dọc, Chéo chính, Chéo phụ
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] d : directions) {
            int count = 1;
            int[][] winningLine = new int[5][2];
            winningLine[0] = new int[]{row, col};

            // Quét tiến
            for (int i = 1; i <= 4; i++) {
                int r = row + d[0] * i;
                int c = col + d[1] * i;
                if (r < 0 || r >= size || c < 0 || c >= size || !symbol.equals(board[r][c])) break;
                winningLine[count++] = new int[]{r, c};
            }

            // Quét lùi
            for (int i = 1; i <= 4; i++) {
                int r = row - d[0] * i;
                int c = col - d[1] * i;
                if (r < 0 || r >= size || c < 0 || c >= size || !symbol.equals(board[r][c])) break;
                winningLine[count++] = new int[]{r, c};
            }

            if (count >= 5) return winningLine;
        }
        return null;
    }

    // --- BỔ SUNG: KIỂM TRA BÀN CỜ ĐÃ ĐẦY CHƯA (ĐIỀU KIỆN HÒA) ---
    public boolean isBoardFull(String[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null || board[i][j].isEmpty()) {
                    return false; // Vẫn còn ít nhất 1 ô trống -> Chưa hòa
                }
            }
        }
        return true; // Đã đánh kín tất cả các ô -> Bàn cờ đầy (Hòa)
    }
}