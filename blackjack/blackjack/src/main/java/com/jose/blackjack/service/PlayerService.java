package com.jose.blackjack.service;

import com.jose.blackjack.model.Player;
import com.jose.blackjack.repos.sql.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final Player player;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, Player player) {
        this.playerRepository = playerRepository;
        this.player = player;
    }

    public Flux<Player> getRanking() {
        return playerRepository.findAllByOrderByScoreAsc();
    }


    public Mono<Player> updatePlayerName(Long playerId, String newName) {
        return playerRepository.findById(playerId)
                .flatMap(player -> {
                    player.setName(newName);
                    return playerRepository.save(player);
                });
    }
}