package com.carogame.backend.service;

import com.carogame.backend.entity.MatchHistory;
import com.carogame.backend.entity.Player;
import com.carogame.backend.repository.MatchHistoryRepository;
import com.carogame.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class GameHistoryService {

    @Autowired
    private MatchHistoryRepository matchRepo;

    @Autowired
    private PlayerRepository playerRepo;

    public void saveGameResult(String roomId, String playerX, String playerO, String winnerSymbol) {
        boolean isDraw = "DRAW".equals(winnerSymbol);

        // 1. Xác định tên người thắng và thua (nếu hòa thì để chuỗi "DRAW" hoặc null)
        String winnerName = isDraw ? "DRAW" : (winnerSymbol.equals("X") ? playerX : playerO);
        String loserName = isDraw ? null : (winnerSymbol.equals("X") ? playerO : playerX);

        // 2. Lưu vào bảng Lịch sử trận đấu
        MatchHistory history = new MatchHistory();
        history.setRoomId(roomId);
        history.setPlayerX(playerX);
        history.setPlayerO(playerO);
        history.setWinner(winnerName); // Nếu hòa sẽ lưu chuỗi "DRAW" vào Database
        history.setEndTime(LocalDateTime.now());
        matchRepo.save(history);

        // KIỂM TRA HÒA: Nếu hòa thì dừng lại, không cộng điểm Thắng/Thua
        if (isDraw) {
            // (Tuỳ chọn: Nếu Entity Player của bạn có thêm trường "draws", bạn có thể cộng 1 điểm hòa cho cả X và O ở đây)
            return;
        }

        // 3. Cộng 1 điểm Thắng cho Winner (Nếu chưa có trong DB thì tạo mới)
        if (winnerName != null) {
            Player winner = playerRepo.findById(winnerName).orElse(new Player());
            if (winner.getPlayerName() == null) winner.setPlayerName(winnerName);
            winner.setWins(winner.getWins() + 1);
            playerRepo.save(winner);
        }

        // 4. Cộng 1 điểm Thua cho Loser (Nếu chưa có trong DB thì tạo mới)
        if (loserName != null) { // Trường hợp phòng chưa có O mà X đã đánh thì bỏ qua
            Player loser = playerRepo.findById(loserName).orElse(new Player());
            if (loser.getPlayerName() == null) loser.setPlayerName(loserName);
            loser.setLosses(loser.getLosses() + 1);
            playerRepo.save(loser);
        }
    }
}