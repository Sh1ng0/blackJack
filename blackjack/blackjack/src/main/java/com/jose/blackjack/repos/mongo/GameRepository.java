package com.jose.blackjack.repos.mongo;

import com.jose.blackjack.model.Game;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends ReactiveMongoRepository<Game, String> {

}

