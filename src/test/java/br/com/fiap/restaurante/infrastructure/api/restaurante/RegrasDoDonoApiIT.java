package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeTipo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Regras do usuário que dependem dos restaurantes dele: exclusão e troca de tipo.
 */
@TesteDeIntegracao
@DisplayName("Regras do dono de restaurante")
class RegrasDoDonoApiIT {

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private long ana;
    private long cantina;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        cantina = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("EXC-07 · excluir usuário responsável por restaurante ativo devolve 409 e não altera nada")
    void deveRecusarExclusaoDeDonoComRestauranteAtivo() {
        api.requisicao().delete("/api/v1/usuarios/{id}", ana)
                .then()
                .statusCode(409)
                .body("detail", equalTo("O usuário " + ana + " é responsável por 1 restaurante ativo. "
                        + "Transfira ou exclua o restaurante antes de excluir o usuário."));
        api.requisicao().get("/api/v1/usuarios/{id}", ana).then().statusCode(200).body("nome", equalTo("Ana Souza"));
    }

    @Test
    @DisplayName("EXC-08 · com os restaurantes todos removidos, o usuário é excluído e os restaurantes seguem apontando para ele")
    void deveExcluirDonoSemRestauranteAtivo() {
        /* arrange */
        api.requisicao().delete("/api/v1/restaurantes/{id}", cantina).then().statusCode(204);

        /* act */
        api.requisicao().delete("/api/v1/usuarios/{id}", ana).then().statusCode(204);

        /* assert */
        assertThat(jdbc.queryForObject("SELECT dono_id FROM restaurante WHERE id = ?", Long.class, cantina))
                .isEqualTo(ana);
        assertThat(jdbc.queryForObject("SELECT nome FROM usuario WHERE id = ?", String.class, ana))
                .isEqualTo("Usuário removido");
    }

    @Test
    @DisplayName("TRO-10 · dono com restaurante ativo não vira Cliente: devolve 409 orientando a transferir ou excluir")
    void deveRecusarTrocaDeDonoComRestauranteAtivo() {
        api.requisicao().body(trocaDeTipo("CLIENTE", "12345678909")).patch("/api/v1/usuarios/{id}/tipo", ana)
                .then()
                .statusCode(409)
                .body("detail", equalTo("O usuário " + ana + " é responsável por 1 restaurante ativo e só deixa de ser "
                        + "Dono de Restaurante depois de transferir ou excluir o restaurante."));
        api.requisicao().get("/api/v1/usuarios/{id}", ana).then().body("tipo", equalTo("DONO_RESTAURANTE"));
    }

    @Test
    @DisplayName("TRO-11 · com os restaurantes todos removidos, o dono vira Cliente")
    void deveTrocarDonoSemRestauranteAtivo() {
        /* arrange */
        api.requisicao().delete("/api/v1/restaurantes/{id}", cantina).then().statusCode(204);

        /* act + assert */
        api.requisicao().body(trocaDeTipo("CLIENTE", "12345678909")).patch("/api/v1/usuarios/{id}/tipo", ana)
                .then().statusCode(200).body("tipo", equalTo("CLIENTE"));
    }
}
