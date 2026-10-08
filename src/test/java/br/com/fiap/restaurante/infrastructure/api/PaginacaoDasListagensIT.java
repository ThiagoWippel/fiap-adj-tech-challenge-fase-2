package br.com.fiap.restaurante.infrastructure.api;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.item;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

/**
 * As regras de paginação valem para todas as listagens; aqui elas rodam em cada
 * uma. Que registros removidos não aparecem nem contam (PAG-05 e PAG-06) fica nos
 * testes de cada listagem, onde há remoção.
 */
@TesteDeIntegracao
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Paginação das listagens")
class PaginacaoDasListagensIT {

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private Map<String, String> rotas;

    @BeforeAll
    void cadastrarDados() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        long ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        api.cadastrar(dono("Carla Dias", "carla@exemplo.com", "carla.dias", "11444777000161"));
        api.cadastrar(cliente("Bruno Lima", "bruno@exemplo.com", "bruno.lima", "52998224725"));
        long bistro = api.cadastrarRestaurante(restaurante("Bistro da Ana", "FRANCESA", ana, "TERCA 18:00 23:00"));
        api.cadastrarRestaurante(restaurante("Adega da Ana", "PORTUGUESA", ana, "QUARTA 18:00 23:00"));
        api.cadastrarItem(bistro, item("Ratatouille", "39.90", false, "fotos/ratatouille.jpg"));
        api.cadastrarItem(bistro, item("Quiche", "25.00", false, "fotos/quiche.jpg"));
        long tipoDono = jdbc.queryForObject("SELECT id FROM tipo_usuario WHERE codigo = 'DONO_RESTAURANTE'", Long.class);
        rotas = Map.of(
                "usuarios v2", "/api/v2/usuarios",
                "tipos de usuário", "/api/v1/tipos-usuario",
                "usuários do tipo", "/api/v1/tipos-usuario/" + tipoDono + "/usuarios",
                "restaurantes", "/api/v1/restaurantes",
                "restaurantes do usuário", "/api/v1/usuarios/" + ana + "/restaurantes",
                "itens do cardápio", "/api/v1/restaurantes/" + bistro + "/itens-cardapio");
    }

    @AfterAll
    void limpar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @ParameterizedTest(name = "PAG-01 · {0}: sem parâmetros, 10 por página, em ordem de nome, com os metadados")
    @ValueSource(strings = {"usuarios v2", "tipos de usuário", "usuários do tipo", "restaurantes",
            "restaurantes do usuário", "itens do cardápio"})
    void deveUsarOPadrao(String listagem) {
        /* act */
        var resposta = api.requisicao().get(rotas.get(listagem)).then().statusCode(200)
                .body("pagina", equalTo(0))
                .body("tamanho", equalTo(10))
                .body("ultima", equalTo(true))
                .extract().jsonPath();

        /* assert */
        List<String> nomes = resposta.getList("conteudo.nome", String.class);
        assertThat(nomes).hasSizeGreaterThanOrEqualTo(2).isSorted();
        assertThat(resposta.getLong("totalElementos")).isEqualTo(nomes.size());
        assertThat(resposta.getInt("totalPaginas")).isEqualTo(1);
    }

    @ParameterizedTest(name = "PAG-02 · {0}: size acima de 50 vira 50")
    @ValueSource(strings = {"usuarios v2", "tipos de usuário", "usuários do tipo", "restaurantes",
            "restaurantes do usuário", "itens do cardápio"})
    void deveLimitarOTamanho(String listagem) {
        api.requisicao().queryParam("size", 500).get(rotas.get(listagem))
                .then().statusCode(200).body("tamanho", equalTo(50));
    }

    @ParameterizedTest(name = "PAG-03 · {0}: sort por campo que não existe devolve 400")
    @ValueSource(strings = {"usuarios v2", "tipos de usuário", "usuários do tipo", "restaurantes",
            "restaurantes do usuário", "itens do cardápio"})
    void deveRecusarOrdenacaoDesconhecida(String listagem) {
        api.requisicao().queryParam("sort", "inexistente").get(rotas.get(listagem))
                .then().statusCode(400).body("title", equalTo("Requisição inválida"));
    }

    @ParameterizedTest(name = "PAG-04 · {0}: página além da última devolve 200 com conteúdo vazio")
    @ValueSource(strings = {"usuarios v2", "tipos de usuário", "usuários do tipo", "restaurantes",
            "restaurantes do usuário", "itens do cardápio"})
    void deveDevolverPaginaVazia(String listagem) {
        api.requisicao().queryParam("page", 9).get(rotas.get(listagem))
                .then().statusCode(200).body("conteudo", empty()).body("ultima", equalTo(true));
    }
}
