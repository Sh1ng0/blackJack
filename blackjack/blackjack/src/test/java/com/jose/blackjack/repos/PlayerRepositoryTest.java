package com.jose.blackjack.repos;
import com.jose.blackjack.model.Player;
import com.jose.blackjack.repos.sql.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest // Asegura que estamos usando la configuración de R2DBC para pruebas
public class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void testCreatePlayer() {
        // Crear un nuevo jugador
        Player player = new Player("John Doe");
        player.addScore(10);

        // Guardar el jugador
        player = playerRepository.save(player).block();

        // Comprobar que el jugador fue guardado
        assertThat(player).isNotNull();
        assertThat(player.getPlayerId()).isNotNull();
        assertThat(player.getName()).isEqualTo("John Doe");
        assertThat(player.getScore()).isEqualTo(10);
    }
}