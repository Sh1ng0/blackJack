package com.jose.blackjack.controller;

import com.jose.blackjack.exceptions.PlayerNotFoundException;
import com.jose.blackjack.model.Game;
import com.jose.blackjack.model.Player;
import com.jose.blackjack.service.GameService;
import com.jose.blackjack.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;

    @Autowired
    public GameController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }


    @Operation(summary = "Crea una nueva partida de Blackjack")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Naisu",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "400", description = "No good")
    })
    @PostMapping("/new")
    public Mono<Game> createNewGame(
            @RequestParam String playerName

    ) {
        return gameService.createGame(playerName);
    }


    @Operation(summary = "Jugar la opción 'Hit'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Naisu",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "404", description = "No good broder")
    })

    @PostMapping("/play/hit")
    public Mono<Game> playHit(

    ) {
        return gameService.hitOption();
    }


    @Operation(summary = "Jugar la opción 'Stand'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stand stand!",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "404", description = "0 patatero")
    })
    @PostMapping("/play/stand")
    public Mono<Game> playStand(

    ) {
        return gameService.standOption();
    }




    @Operation(summary = "Jugar la opción 'Double'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Here comes the money",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "404", description = "Errol")
    })
    @PostMapping("/play/double")
    public Mono<Game> playDouble(

    ) {
        return gameService.doubleOption();
    }



    @Operation(summary = "Obtener el ranking de jugadores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking obtenido exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Player.class)))
    })
    @GetMapping("/ranking")
    public Flux<Player> getRanking() {
        return playerService.getRanking();
    }


    @Operation(summary = "Actualizar el nombre de un jugador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nombre actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Player.class))),
            @ApiResponse(responseCode = "404", description = "Jugador no encontrado")
    })

    //TODO hacer el Swaggah para este method
    @PutMapping("player/{playerId}")
    public Mono<Player> updatePlayerName(@PathVariable Long playerId, @RequestBody String newName) {
        return playerService.updatePlayerName(playerId, newName)
                .switchIfEmpty(Mono.error(new PlayerNotFoundException("Player not found")));
    }
}