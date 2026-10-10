package br.com.fiap.restaurante.infrastructure.api.itemcardapio;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.item;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@TesteDeIntegracao
@DisplayName("API de itens do cardápio")
class ItemCardapioApiIT {

    private static final String ITENS = "/api/v1/restaurantes/{restauranteId}/itens-cardapio";
    private static final String ITEM = ITENS + "/{itemId}";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private long cantina;
    private long sushi;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        long ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        cantina = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
        sushi = api.cadastrarRestaurante(restaurante("Sushi Bar", "JAPONESA", ana, "TERCA 18:00 23:00"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("ITE-21 · ITE-07 · POST devolve 201 com Location e o preço em duas casas (39.9 vira 39.90)")
    void deveCadastrar() {
        /* act */
        Response resposta = api.requisicao()
                .body(item("  Feijoada   completa ", "39.9", true, "fotos/feijoada.jpg"))
                .post(ITENS, cantina);

        /* assert */
        resposta.then()
                .statusCode(201)
                .body("nome", equalTo("Feijoada completa"))
                .body("restauranteId", equalTo((int) cantina))
                .body("apenasNoLocal", equalTo(true))
                .body("caminhoFoto", equalTo("fotos/feijoada.jpg"));
        long id = resposta.jsonPath().getLong("id");
        assertThat(resposta.asString()).contains("\"preco\":39.90");
        assertThat(resposta.header("Location")).endsWith("/api/v1/restaurantes/" + cantina + "/itens-cardapio/" + id);
        assertThat(jdbc.queryForObject("SELECT preco FROM item_cardapio WHERE id = ?", BigDecimal.class, id))
                .isEqualTo(new BigDecimal("39.90"));
    }

    @Test
    @DisplayName("ITE-09 · ITE-05 · disponibilidade ausente ou preço com três casas devolve 400 apontando o campo")
    void deveRecusarCamposInvalidos() {
        api.requisicao().body("""
                        { "nome": "Feijoada", "descricao": "Feijoada completa.", "preco": 39.90,
                          "caminhoFoto": "fotos/feijoada.jpg" }""")
                .post(ITENS, cantina)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItem("apenasNoLocal"));
        api.requisicao().body(item("Feijoada", "39.999", true, "fotos/feijoada.jpg")).post(ITENS, cantina)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItem("preco"));
        api.requisicao().body(item("Feijoada", "39.90", true, "fotos/feijoada.gif")).post(ITENS, cantina)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItem("caminhoFoto"));
    }

    @Test
    @DisplayName("ITE-23 · caminho de foto que sobe de pasta ou usa esquema que não é http devolve 400")
    void deveRecusarCaminhoDeFotoForaDoPadrao() {
        api.requisicao().body(item("Feijoada", "39.90", true, "../../etc/feijoada.png")).post(ITENS, cantina)
                .then()
                .statusCode(400)
                .body("detail", equalTo("O caminho da foto não pode subir de pasta com \"..\"."));
        api.requisicao().body(item("Feijoada", "39.90", true, "javascript:alert(1).png")).post(ITENS, cantina)
                .then()
                .statusCode(400)
                .body("detail", equalTo("A foto deve ser um caminho relativo ou uma URL http ou https."));
    }

    @Test
    @DisplayName("ITE-12 · ITE-13 · nome de outro item ativo, mesmo com maiúsculas, acentos ou espaço no fim diferentes, devolve 409")
    void deveRecusarNomeRepetido() {
        /* arrange */
        api.cadastrarItem(cantina, item("Feijão Tropeiro", "35.00", false, "fotos/tropeiro.png"));

        /* act + assert */
        api.requisicao().body(item("Feijão Tropeiro", "35.00", false, "fotos/tropeiro.png")).post(ITENS, cantina)
                .then()
                .statusCode(409)
                .body("detail", equalTo("O restaurante " + cantina + " já tem um item ativo chamado Feijão Tropeiro."));
        api.requisicao().body(item("FEIJAO TROPEIRO ", "35.00", false, "fotos/tropeiro.png")).post(ITENS, cantina)
                .then().statusCode(409);
    }

    @Test
    @DisplayName("ITE-24 · nomes que só diferem no emoji são itens diferentes; maiúscula ainda não diferencia")
    void deveDistinguirNomesQueSoDiferemNoEmoji() {
        /* arrange */
        api.cadastrarItem(cantina, item("Pizza 🍕", "45.00", false, "fotos/pizza.jpg"));

        /* act + assert */
        api.requisicao().body(item("Pizza 🍔", "45.00", false, "fotos/pizza.jpg")).post(ITENS, cantina)
                .then().statusCode(201);
        api.requisicao().body(item("PIZZA 🍕", "45.00", false, "fotos/pizza.jpg")).post(ITENS, cantina)
                .then().statusCode(409);
    }

    @Test
    @DisplayName("ITE-14 · o mesmo nome em outro restaurante é aceito")
    void deveAceitarMesmoNomeEmOutroRestaurante() {
        /* arrange */
        api.cadastrarItem(cantina, item("Salada da casa", "25.00", false, "fotos/salada.webp"));

        /* act + assert */
        api.requisicao().body(item("Salada da casa", "28.00", false, "fotos/salada.webp")).post(ITENS, sushi)
                .then().statusCode(201).body("restauranteId", equalTo((int) sushi));
    }

    @Test
    @DisplayName("ITE-21 · GET lista paginada em ordem de nome, com filtro apenasNoLocal")
    void deveListar() {
        /* arrange */
        api.cadastrarItem(cantina, item("Tiramisù", "22.00", false, "fotos/tiramisu.jpg"));
        api.cadastrarItem(cantina, item("Lasanha", "48.00", true, "fotos/lasanha.jpg"));
        api.cadastrarItem(sushi, item("Temaki", "30.00", false, "fotos/temaki.jpg"));

        /* act + assert */
        api.requisicao().get(ITENS, cantina)
                .then()
                .statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Lasanha", "Tiramisù")))
                .body("totalElementos", equalTo(2));
        api.requisicao().queryParam("apenasNoLocal", true).get(ITENS, cantina)
                .then().body("conteudo.nome", equalTo(List.of("Lasanha")));
        api.requisicao().queryParam("sort", "preco,desc").get(ITENS, cantina)
                .then().body("conteudo.nome", equalTo(List.of("Lasanha", "Tiramisù")));
        api.requisicao().queryParam("sort", "restauranteId").get(ITENS, cantina).then().statusCode(400);
    }

    @Test
    @DisplayName("ITE-21 · ITE-15 · GET por id e PUT mantendo o nome devolvem 200")
    void deveConsultarEAtualizar() {
        /* arrange */
        long id = api.cadastrarItem(cantina, item("Lasanha", "48.00", true, "fotos/lasanha.jpg"));
        LocalDateTime janeiro = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
        jdbc.update("UPDATE item_cardapio SET data_criacao = ?, data_ultima_alteracao = ? WHERE id = ?", janeiro, janeiro, id);

        /* act */
        Response resposta = api.requisicao().body(item("Lasanha", "52.5", false, "fotos/lasanha-grande.png"))
                .put(ITEM, cantina, id);

        /* assert */
        api.requisicao().get(ITEM, cantina, id).then().statusCode(200).body("nome", equalTo("Lasanha"));
        resposta.then()
                .statusCode(200)
                .body("apenasNoLocal", equalTo(false))
                .body("dataCriacao", equalTo("2026-01-10T09:00:00"));
        assertThat(resposta.asString()).contains("\"preco\":52.50");
        assertThat(LocalDateTime.parse(resposta.jsonPath().getString("dataUltimaAlteracao"))).isAfter(janeiro);
    }

    @Test
    @DisplayName("ITE-16 · PUT com o nome de outro item ativo devolve 409")
    void deveRecusarRenomearParaNomeDeOutroItem() {
        /* arrange */
        api.cadastrarItem(cantina, item("Lasanha", "48.00", true, "fotos/lasanha.jpg"));
        long tiramisu = api.cadastrarItem(cantina, item("Tiramisù", "22.00", false, "fotos/tiramisu.jpg"));

        /* act + assert */
        api.requisicao().body(item("lasanha", "22.00", false, "fotos/tiramisu.jpg")).put(ITEM, cantina, tiramisu)
                .then().statusCode(409);
    }

    @Test
    @DisplayName("ITE-17 · pela rota de outro restaurante, o item devolve 404 em todas as operações")
    void deveEsconderItemDeOutroRestaurante() {
        /* arrange */
        long id = api.cadastrarItem(cantina, item("Lasanha", "48.00", true, "fotos/lasanha.jpg"));

        /* act + assert */
        api.requisicao().get(ITEM, sushi, id)
                .then()
                .statusCode(404)
                .body("detail", equalTo("Item " + id + " não encontrado no restaurante " + sushi + "."));
        api.requisicao().body(item("Lasanha", "48.00", true, "fotos/lasanha.jpg")).put(ITEM, sushi, id)
                .then().statusCode(404);
        api.requisicao().delete(ITEM, sushi, id).then().statusCode(404);
        api.requisicao().get(ITEM, cantina, id).then().statusCode(200);
    }

    @Test
    @DisplayName("ITE-18 · ITE-19 · DELETE devolve 204, a linha fica com removido_em e o nome pode ser usado de novo")
    void deveExcluirLogicamente() {
        /* arrange */
        long id = api.cadastrarItem(cantina, item("Lasanha", "48.00", true, "fotos/lasanha.jpg"));

        /* act */
        api.requisicao().delete(ITEM, cantina, id).then().statusCode(204).body(equalTo(""));

        /* assert */
        assertThat(jdbc.queryForObject("SELECT removido_em FROM item_cardapio WHERE id = ?", LocalDateTime.class, id))
                .isNotNull();
        api.requisicao().get(ITEM, cantina, id).then().statusCode(404);
        api.requisicao().get(ITENS, cantina).then().body("conteudo", empty());
        api.requisicao().delete(ITEM, cantina, id).then().statusCode(404);
        long novo = api.cadastrarItem(cantina, item("Lasanha", "50.00", true, "fotos/lasanha.jpg"));
        assertThat(novo).isNotEqualTo(id);
    }

    @Test
    @DisplayName("ITE-22 · com o restaurante removido, todas as rotas de item devolvem 404")
    void deveEsconderItensDeRestauranteRemovido() {
        /* arrange */
        long id = api.cadastrarItem(cantina, item("Lasanha", "48.00", true, "fotos/lasanha.jpg"));
        api.requisicao().delete("/api/v1/restaurantes/{id}", cantina).then().statusCode(204);

        /* act + assert */
        api.requisicao().get(ITENS, cantina).then().statusCode(404);
        api.requisicao().get(ITEM, cantina, id).then().statusCode(404);
        api.requisicao().body(item("Pizza", "40.00", false, "fotos/pizza.jpg")).post(ITENS, cantina)
                .then().statusCode(404).body("detail", equalTo("Restaurante " + cantina + " não encontrado."));
        api.requisicao().body(item("Lasanha", "48.00", true, "fotos/lasanha.jpg")).put(ITEM, cantina, id)
                .then().statusCode(404);
        api.requisicao().delete(ITEM, cantina, id).then().statusCode(404);
    }
}
