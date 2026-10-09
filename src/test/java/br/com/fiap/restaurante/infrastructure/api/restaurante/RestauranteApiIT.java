package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@TesteDeIntegracao
@DisplayName("API de restaurantes")
class RestauranteApiIT {

    private static final String RESTAURANTES = "/api/v1/restaurantes";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private long ana;
    private long joao;
    private long maria;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        joao = api.cadastrar(dono("João Pereira", "joao@exemplo.com", "joao.pereira", "11444777000161"));
        maria = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("RES-12 · HOR-11 · POST devolve 201, Location, o dono como {id, nome} e os turnos ordenados")
    void deveCadastrar() {
        /* act */
        Response resposta = api.requisicao()
                .body(restaurante("Cantina da Nona", "ITALIANA", ana, "SEXTA 18:00 02:00", "SEGUNDA 18:00 23:00",
                        "SEGUNDA 11:00 15:00"))
                .post(RESTAURANTES);

        /* assert */
        resposta.then()
                .statusCode(201)
                .body("nome", equalTo("Cantina da Nona"))
                .body("tipoCozinha", equalTo("ITALIANA"))
                .body("dono", equalTo(Map.of("id", (int) ana, "nome", "Ana Souza")))
                .body("horarios.diaSemana", equalTo(List.of("SEGUNDA", "SEGUNDA", "SEXTA")))
                .body("horarios.abertura", equalTo(List.of("11:00", "18:00", "18:00")))
                .body("horarios.fechamento", equalTo(List.of("15:00", "23:00", "02:00")))
                .body("endereco.cep", equalTo("88301000"));
        assertThat(resposta.header("Location")).endsWith(RESTAURANTES + "/" + resposta.jsonPath().getLong("id"));
    }

    @Test
    @DisplayName("RES-07 · POST com dono do tipo Cliente devolve 409, orientando a trocar o tipo; dono inexistente, 404")
    void deveRecusarDonoInvalido() {
        api.requisicao().body(restaurante("Cantina", "ITALIANA", maria, "SEGUNDA 11:00 15:00")).post(RESTAURANTES)
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflito de dados"))
                .body("detail", equalTo("O usuário " + maria
                        + " não é Dono de Restaurante. Troque o tipo dele antes de cadastrar o restaurante."));
        api.requisicao().body(restaurante("Cantina", "ITALIANA", 999999, "SEGUNDA 11:00 15:00")).post(RESTAURANTES)
                .then().statusCode(404);
    }

    @Test
    @DisplayName("HOR-05 · turnos sobrepostos devolvem 400 com os dois turnos na mensagem")
    void deveRecusarTurnosSobrepostos() {
        api.requisicao()
                .body(restaurante("Cantina", "ITALIANA", ana, "SEXTA 18:00 02:00", "SABADO 01:00 10:00"))
                .post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("title", equalTo("Regra de negócio violada"))
                .body("detail", equalTo("Os turnos SEXTA 18:00–02:00 e SABADO 01:00–10:00 se sobrepõem."));
    }

    @Test
    @DisplayName("HOR-13 · horário fora do formato HH:mm ou dia inexistente devolve 400")
    void deveRecusarHorarioOuDiaInvalido() {
        api.requisicao().body(restaurante("Cantina", "ITALIANA", ana, "SEGUNDA 25:00 15:00", "TERCA 11h 15:00"))
                .post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItems("horarios[0].abertura", "horarios[1].abertura"));
        api.requisicao().body(restaurante("Cantina", "ITALIANA", ana, "FERIADO 11:00 15:00")).post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("detail", containsString("O dia FERIADO não existe."));
    }

    @Test
    @DisplayName("HOR-14 · lista de turnos com um elemento nulo devolve 400 apontando o turno, e não 500")
    void deveRecusarTurnoNulo() {
        /* arrange */
        String corpo = restaurante("Cantina", "ITALIANA", ana).replace("\"horarios\": []", "\"horarios\": [null]");

        /* act + assert */
        api.requisicao().body(corpo).post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItems("horarios[0]"));
    }

    @Test
    @DisplayName("HOR-15 · mais de 50 turnos devolve 400 apontando o campo")
    void deveRecusarMaisDe50Turnos() {
        /* arrange */
        String[] dias = {"SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO"};
        String[] turnos = new String[51];
        for (int i = 0; i < turnos.length; i++) {
            turnos[i] = "%s %02d:00 %02d:00".formatted(dias[i % 7], i / 7, i / 7 + 1);
        }

        /* act + assert */
        api.requisicao().body(restaurante("Cantina", "ITALIANA", ana, turnos)).post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItems("horarios"));
    }

    @Test
    @DisplayName("HOR-10 · COZ-02 · sem turnos ou sem tipo de cozinha devolve 400 apontando o campo")
    void deveExigirTurnosETipoDeCozinha() {
        api.requisicao().body(restaurante("Cantina", "", ana)).post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItems("horarios", "tipoCozinha"));
    }

    @Test
    @DisplayName("COZ-01 · tipo de cozinha fora da lista devolve 400 com os valores aceitos")
    void deveRecusarTipoDeCozinhaInvalido() {
        api.requisicao().body(restaurante("Cantina", "TAILANDESA", ana, "SEGUNDA 11:00 15:00")).post(RESTAURANTES)
                .then()
                .statusCode(400)
                .body("detail", containsString("Valores aceitos: BRASILEIRA, ITALIANA, PIZZARIA"));
    }

    @Test
    @DisplayName("RES-13 · COZ-03 · GET filtra por trecho do nome, sem diferenciar maiúsculas, e por tipo de cozinha")
    void deveListarComFiltros() {
        /* arrange */
        api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
        api.cadastrarRestaurante(restaurante("Nona Pizza", "PIZZARIA", ana, "SEGUNDA 18:00 23:00"));
        api.cadastrarRestaurante(restaurante("Sushi Bar", "JAPONESA", joao, "TERCA 18:00 23:00"));

        /* act + assert */
        api.requisicao().get(RESTAURANTES)
                .then().statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Cantina da Nona", "Nona Pizza", "Sushi Bar")))
                .body("totalElementos", equalTo(3));
        api.requisicao().queryParam("nome", "NONA").get(RESTAURANTES)
                .then().body("conteudo.nome", equalTo(List.of("Cantina da Nona", "Nona Pizza")));
        api.requisicao().queryParam("tipoCozinha", "pizzaria").get(RESTAURANTES)
                .then().body("conteudo.nome", equalTo(List.of("Nona Pizza")));
        api.requisicao().queryParam("nome", "nona").queryParam("tipoCozinha", "ITALIANA").get(RESTAURANTES)
                .then().body("conteudo.nome", equalTo(List.of("Cantina da Nona")));
        api.requisicao().queryParam("tipoCozinha", "TAILANDESA").get(RESTAURANTES).then().statusCode(400);
        api.requisicao().queryParam("sort", "dono").get(RESTAURANTES).then().statusCode(400);
    }

    @Test
    @DisplayName("RES-14 · GET por id devolve 200; inexistente devolve 404")
    void deveBuscarPorId() {
        /* arrange */
        long id = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));

        /* act + assert */
        api.requisicao().get(RESTAURANTES + "/{id}", id).then().statusCode(200).body("dono.nome", equalTo("Ana Souza"));
        api.requisicao().get(RESTAURANTES + "/999999")
                .then().statusCode(404).body("detail", equalTo("Restaurante 999999 não encontrado."));
    }

    @Test
    @DisplayName("RES-15 · HOR-12 · PUT devolve 200, mantém dataCriacao, avança dataUltimaAlteracao e substitui os turnos")
    void deveAtualizar() {
        /* arrange */
        long id = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00",
                "SEGUNDA 18:00 23:00"));
        LocalDateTime janeiro = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
        jdbc.update("UPDATE restaurante SET data_criacao = ?, data_ultima_alteracao = ? WHERE id = ?", janeiro, janeiro, id);
        List<Long> turnosAntigos = idsDosTurnos(id);

        /* act */
        Response resposta = api.requisicao()
                .body(restaurante("Pizzaria da Nona", "PIZZARIA", ana, "SEXTA 18:00 02:00"))
                .put(RESTAURANTES + "/{id}", id);

        /* assert */
        resposta.then()
                .statusCode(200)
                .body("nome", equalTo("Pizzaria da Nona"))
                .body("tipoCozinha", equalTo("PIZZARIA"))
                .body("horarios.diaSemana", equalTo(List.of("SEXTA")))
                .body("dataCriacao", equalTo("2026-01-10T09:00:00"));
        assertThat(LocalDateTime.parse(resposta.jsonPath().getString("dataUltimaAlteracao"))).isAfter(janeiro);
        assertThat(idsDosTurnos(id)).hasSize(1).doesNotContainAnyElementsOf(turnosAntigos);
    }

    @Test
    @DisplayName("HOR-12 · um PUT que muda só os turnos também avança dataUltimaAlteracao")
    void deveRegistrarAlteracaoSoDosTurnos() {
        /* arrange */
        long id = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
        LocalDateTime janeiro = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
        jdbc.update("UPDATE restaurante SET data_criacao = ?, data_ultima_alteracao = ? WHERE id = ?", janeiro, janeiro, id);

        /* act */
        Response resposta = api.requisicao()
                .body(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 18:00 23:00"))
                .put(RESTAURANTES + "/{id}", id);

        /* assert */
        resposta.then().statusCode(200).body("horarios.abertura", equalTo(List.of("18:00")));
        assertThat(LocalDateTime.parse(resposta.jsonPath().getString("dataUltimaAlteracao"))).isAfter(janeiro);
    }

    @Test
    @DisplayName("RES-09 · PUT transfere para outro Dono de Restaurante; para um Cliente, devolve 409")
    void deveTransferirODono() {
        /* arrange */
        long id = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));

        /* act + assert */
        api.requisicao().body(restaurante("Cantina da Nona", "ITALIANA", maria, "SEGUNDA 11:00 15:00"))
                .put(RESTAURANTES + "/{id}", id)
                .then().statusCode(409);
        api.requisicao().body(restaurante("Cantina da Nona", "ITALIANA", joao, "SEGUNDA 11:00 15:00"))
                .put(RESTAURANTES + "/{id}", id)
                .then().statusCode(200).body("dono.id", equalTo((int) joao));
        api.requisicao().body(restaurante("Cantina da Nona", "ITALIANA", joao, "SEGUNDA 11:00 15:00"))
                .put(RESTAURANTES + "/999999")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("RES-11 · RES-16 · DELETE devolve 204; a linha fica com removido_em e o restaurante some do GET e da lista")
    void deveExcluirLogicamente() {
        /* arrange */
        long id = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));

        /* act */
        api.requisicao().delete(RESTAURANTES + "/{id}", id).then().statusCode(204).body(equalTo(""));

        /* assert */
        assertThat(jdbc.queryForObject("SELECT removido_em FROM restaurante WHERE id = ?", LocalDateTime.class, id))
                .isNotNull();
        assertThat(idsDosTurnos(id)).hasSize(1);
        api.requisicao().get(RESTAURANTES + "/{id}", id).then().statusCode(404);
        api.requisicao().get(RESTAURANTES).then().body("conteudo", empty());
        api.requisicao().delete(RESTAURANTES + "/{id}", id).then().statusCode(404);
    }

    @Test
    @DisplayName("RES-17 · GET /usuarios/{id}/restaurantes lista, paginados, só os restaurantes ativos do usuário")
    void deveListarOsRestaurantesDoUsuario() {
        /* arrange */
        api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
        long removido = api.cadastrarRestaurante(restaurante("Nona Pizza", "PIZZARIA", ana, "SEGUNDA 18:00 23:00"));
        api.cadastrarRestaurante(restaurante("Sushi Bar", "JAPONESA", joao, "TERCA 18:00 23:00"));
        api.requisicao().delete(RESTAURANTES + "/{id}", removido).then().statusCode(204);

        /* act + assert */
        api.requisicao().get("/api/v1/usuarios/{id}/restaurantes", ana)
                .then()
                .statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Cantina da Nona")))
                .body("totalElementos", equalTo(1));
        api.requisicao().get("/api/v1/usuarios/999999/restaurantes").then().statusCode(404);
    }

    @Test
    @DisplayName("RES-12 · o campo removido_em e a senha do dono nunca aparecem na resposta")
    void naoDeveExporDadosInternos() {
        /* act */
        String corpo = api.requisicao()
                .body(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"))
                .post(RESTAURANTES).asString();

        /* assert */
        assertThat(corpo).doesNotContain("removido").doesNotContain("senha").doesNotContain("email");
    }

    private List<Long> idsDosTurnos(long restauranteId) {
        return jdbc.queryForList("SELECT id FROM horario_funcionamento WHERE restaurante_id = ?", Long.class,
                restauranteId);
    }
}
