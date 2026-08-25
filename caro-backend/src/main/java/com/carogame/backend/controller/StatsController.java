package com.carogame.backend.controller;

import com.carogame.backend.entity.MatchHistory;
import com.carogame.backend.entity.Player;
import com.carogame.backend.repository.MatchHistoryRepository;
import com.carogame.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private MatchHistoryRepository matchRepo;

    // Lấy Top 5 người chơi thắng nhiều nhất
    @GetMapping("/leaderboard")
    public List<Player> getLeaderboard() {
        return playerRepo.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "wins"))).getContent();
    }

    // Lấy 10 trận đấu gần nhất
    @GetMapping("/history")
    public List<MatchHistory> getRecentHistory() {
        return matchRepo.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "endTime"))).getContent();
    }
}