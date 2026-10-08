package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Conta as consultas ao banco feitas pela listagem de restaurantes, com as
 * estatísticas do Hibernate (ligadas só no perfil de teste).
 */
@TesteDeIntegracao
@DisplayName("Consultas da listagem de restaurantes")
class ConsultasDeRestauranteIT {

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManagerFactory fabrica;

    private ApiDeTeste api;
    private long ana;
    private long joao;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        joao = api.cadastrar(dono("João Pereira", "joao@exemplo.com", "joao.pereira", "11444777000161"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("RES-18 · listar restaurantes com o dono e os turnos usa o mesmo número de consultas para 2 ou 6 restaurantes")
    void deveListarSemConsultaExtraPorRestaurante() {
        /* arrange */
        cadastrarRestaurantes(2);
        long comDois = consultasDaListagem();
        cadastrarRestaurantes(4);

        /* act */
        long comSeis = consultasDaListagem();

        /* assert */
        assertThat(comSeis).isEqualTo(comDois);
        assertThat(comSeis).isLessThanOrEqualTo(4);
    }

    private long consultasDaListagem() {
        Statistics estatisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();
        api.requisicao().get("/api/v1/restaurantes?size=50").then().statusCode(200);
        return estatisticas.getPrepareStatementCount();
    }

    private void cadastrarRestaurantes(int quantidade) {
        int existentes = jdbc.queryForObject("SELECT COUNT(*) FROM restaurante", Integer.class);
        for (int i = 1; i <= quantidade; i++) {
            long dono = i % 2 == 0 ? ana : joao;
            api.cadastrarRestaurante(restaurante("Restaurante " + (existentes + i), "BRASILEIRA", dono,
                    "SEGUNDA 11:00 15:00", "SEXTA 18:00 02:00"));
        }
    }
}
