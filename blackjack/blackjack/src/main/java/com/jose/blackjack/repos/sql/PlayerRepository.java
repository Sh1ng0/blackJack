package com.jose.blackjack.repos.sql;

import com.jose.blackjack.model.Player;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PlayerRepository extends R2dbcRepository<Player, Long> {
    Flux<Player> findAllByOrderByScoreAsc();
    Mono<Player> findById(Long playerId);
}