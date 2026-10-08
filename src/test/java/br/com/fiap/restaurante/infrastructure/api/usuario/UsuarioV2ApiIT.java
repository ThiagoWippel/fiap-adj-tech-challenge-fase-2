package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

/**
 * Busca paginada da v2. Doze usuários, cadastrados fora da ordem alfabética.
 */
@TesteDeIntegracao
@DisplayName("API de usuários (v2) e paginação")
class UsuarioV2ApiIT {

    private static final String USUARIOS_V2 = "/api/v2/usuarios";

    private static final String[][] USUARIOS = {
            {"Gabriela Reis", "gabriela.reis", "52998224725"},
            {"Ana Souza", "ana.souza", "12345678909"},
            {"Lucas Teixeira", "lucas.teixeira", "11144477735"},
            {"Diego Rocha", "diego.rocha", "39053344705"},
            {"Íris Campos", "iris.campos", "86288366757"},
            {"Bruno Lima", "bruno.lima", "93541134780"},
            {"Karina Alves", "karina.alves", "74697131401"},
            {"Fábio Nunes", "fabio.nunes", "28475263585"},
            {"Carla Dias", "carla.dias", "16899535009"},
            {"João Pereira", "joao.pereira", "07098772003"},
            {"Heitor Melo", "heitor.melo", "45317828791"},
            {"Elisa Prado", "elisa.prado", "63210876533"}
    };

    private static final List<String> PRIMEIRA_PAGINA = List.of("Ana Souza", "Bruno Lima", "Carla Dias",
            "Diego Rocha", "Elisa Prado", "Fábio Nunes", "Gabriela Reis", "Heitor Melo", "Íris Campos", "João Pereira");

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        for (String[] usuario : USUARIOS) {
            api.cadastrar(cliente(usuario[0], usuario[1] + "@exemplo.com", usuario[1], usuario[2]));
        }
    }

    @Test
    @DisplayName("USU-28 · GET /api/v2/usuarios?nome= devolve um objeto com conteudo e os metadados da página")
    void deveDevolverAPaginaFiltradaPeloNome() {
        api.requisicao()
        .when()
                .get(USUARIOS_V2 + "?nome=SOUZA")
        .then()
                .statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Ana Souza")))
                .body("conteudo[0]", not(hasKey("senha")))
                .body("pagina", equalTo(0))
                .body("tamanho", equalTo(10))
                .body("totalElementos", equalTo(1))
                .body("totalPaginas", equalTo(1))
                .body("ultima", equalTo(true));
    }

    @Test
    @DisplayName("PAG-01 · sem parâmetros, vêm 10 itens por página, ordenados por nome")
    void deveUsarOPadraoDeDezPorPaginaOrdenadoPorNome() {
        api.requisicao()
        .when()
                .get(USUARIOS_V2)
        .then()
                .statusCode(200)
                .body("conteudo.nome", equalTo(PRIMEIRA_PAGINA))
                .body("tamanho", equalTo(10))
                .body("totalElementos", equalTo(12))
                .body("totalPaginas", equalTo(2))
                .body("ultima", equalTo(false));
    }

    @Test
    @DisplayName("PAG-01 · a segunda página traz o restante, e a ordenação pode ser decrescente")
    void devePaginarEOrdenarDecrescente() {
        api.requisicao().get(USUARIOS_V2 + "?page=1")
                .then().statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Karina Alves", "Lucas Teixeira")))
                .body("ultima", equalTo(true));
        api.requisicao().get(USUARIOS_V2 + "?size=3&sort=nome,desc")
                .then().statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Lucas Teixeira", "Karina Alves", "João Pereira")));
    }

    @Test
    @DisplayName("PAG-02 · size acima de 50 é limitado a 50, e os metadados mostram o tamanho efetivo")
    void deveLimitarOTamanhoDaPagina() {
        api.requisicao()
        .when()
                .get(USUARIOS_V2 + "?size=500")
        .then()
                .statusCode(200)
                .body("tamanho", equalTo(50))
                .body("conteudo", hasSize(12));
    }

    @Test
    @DisplayName("PAG-03 · sort por um campo que não existe, ou que não pode ser ordenado, devolve 400")
    void deveRecusarOrdenacaoDesconhecida() {
        api.requisicao().get(USUARIOS_V2 + "?sort=inexistente")
                .then()
                .statusCode(400)
                .body("title", equalTo("Requisição inválida"))
                .body("detail", equalTo("Não é possível ordenar por inexistente. "
                        + "Campos aceitos: dataCriacao, dataUltimaAlteracao, email, id, login, nome."));
        api.requisicao().get(USUARIOS_V2 + "?sort=senha").then().statusCode(400);
    }

    @Test
    @DisplayName("PAG-04 · uma página além da última devolve 200 com conteúdo vazio")
    void deveDevolverPaginaVaziaAlemDaUltima() {
        api.requisicao()
        .when()
                .get(USUARIOS_V2 + "?page=9")
        .then()
                .statusCode(200)
                .body("conteudo", empty())
                .body("totalElementos", equalTo(12))
                .body("ultima", equalTo(true));
    }

    @Test
    @DisplayName("PAG-05 · PAG-06 · usuários removidos não aparecem e não entram na contagem")
    void deveIgnorarUsuariosRemovidos() {
        /* arrange */
        long ana = api.requisicao().queryParam("nome", "Ana Souza").get("/api/v1/usuarios").jsonPath().getLong("[0].id");
        api.requisicao().delete("/api/v1/usuarios/{id}", ana).then().statusCode(204);

        /* act + assert */
        api.requisicao().get(USUARIOS_V2)
                .then()
                .statusCode(200)
                .body("conteudo[0].nome", equalTo("Bruno Lima"))
                .body("totalElementos", equalTo(11));
    }
}
