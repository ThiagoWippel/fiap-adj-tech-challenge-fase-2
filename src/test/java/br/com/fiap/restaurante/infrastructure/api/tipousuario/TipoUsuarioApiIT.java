package br.com.fiap.restaurante.infrastructure.api.tipousuario;

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

import java.util.List;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.tipo;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.usuarioComCpf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@TesteDeIntegracao
@DisplayName("API de tipos de usuário")
class TipoUsuarioApiIT {

    private static final String TIPOS = "/api/v1/tipos-usuario";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("TIP-16 · POST devolve 201, Location e o código gerado do nome")
    void deveCadastrarTipo() {
        /* act */
        Response resposta = api.requisicao().body(tipo("Ajudante de Cozinha")).post(TIPOS);

        /* assert */
        resposta.then()
                .statusCode(201)
                .body("nome", equalTo("Ajudante de Cozinha"))
                .body("codigo", equalTo("AJUDANTE_DE_COZINHA"))
                .body("sistema", equalTo(false));
        assertThat(resposta.header("Location")).endsWith(TIPOS + "/" + resposta.jsonPath().getLong("id"));
    }

    @Test
    @DisplayName("TIP-16 · POST com nome fora de 3 a 50 caracteres devolve 400 apontando o campo")
    void deveRecusarNomeInvalido() {
        api.requisicao().body(tipo("Ab")).post(TIPOS)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItem("nome"));
    }

    @Test
    @DisplayName("TIP-06 · POST com nome que só muda nas maiúsculas ou nos acentos devolve 409")
    void deveRecusarNomeRepetido() {
        /* arrange */
        api.cadastrarTipo("Gerência");

        /* act + assert */
        api.requisicao().body(tipo("GERENCIA")).post(TIPOS)
                .then()
                .statusCode(409)
                .body("detail", equalTo("Já existe um tipo de usuário com o nome GERENCIA."));
    }

    @Test
    @DisplayName("TIP-07 · POST com nome que gera o código de outro tipo devolve 409")
    void deveRecusarCodigoRepetido() {
        /* arrange */
        api.cadastrarTipo("Ajudante de Cozinha");

        /* act + assert */
        api.requisicao().body(tipo("Ajudante-de-Cozinha")).post(TIPOS)
                .then()
                .statusCode(409)
                .body("detail", equalTo("O nome Ajudante-de-Cozinha gera o código AJUDANTE_DE_COZINHA, "
                        + "que já pertence ao tipo Ajudante de Cozinha."));
    }

    @Test
    @DisplayName("TIP-16 · GET lista paginada em ordem de nome; sort por campo não aceito devolve 400")
    void deveListarPaginado() {
        /* arrange */
        api.cadastrarTipo("Entregador");

        /* act + assert */
        api.requisicao().get(TIPOS)
                .then()
                .statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Cliente", "Dono de Restaurante", "Entregador")))
                .body("conteudo.sistema", equalTo(List.of(true, true, false)))
                .body("tamanho", equalTo(10))
                .body("totalElementos", equalTo(3));
        api.requisicao().get(TIPOS + "?sort=senha").then().statusCode(400);
    }

    @Test
    @DisplayName("TIP-16 · GET por id devolve 200; id inexistente devolve 404")
    void deveBuscarPorId() {
        /* arrange */
        long id = api.cadastrarTipo("Entregador");

        /* act + assert */
        api.requisicao().get(TIPOS + "/{id}", id).then().statusCode(200).body("codigo", equalTo("ENTREGADOR"));
        api.requisicao().get(TIPOS + "/999999")
                .then()
                .statusCode(404)
                .body("detail", equalTo("Tipo de usuário 999999 não encontrado."));
    }

    @Test
    @DisplayName("TIP-16 · PUT renomeia mantendo o código; 404 para tipo inexistente, 409 para nome de outro tipo")
    void deveRenomear() {
        /* arrange */
        long id = api.cadastrarTipo("Entregador");

        /* act + assert */
        api.requisicao().body(tipo("Entregador Parceiro")).put(TIPOS + "/{id}", id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Entregador Parceiro"))
                .body("codigo", equalTo("ENTREGADOR"));
        api.requisicao().body(tipo("Qualquer Nome")).put(TIPOS + "/999999").then().statusCode(404);
        api.requisicao().body(tipo("cliente")).put(TIPOS + "/{id}", id).then().statusCode(409);
        api.requisicao().body(tipo("Ab")).put(TIPOS + "/{id}", id).then().statusCode(400);
    }

    @Test
    @DisplayName("TIP-09 · renomear um tipo de sistema é permitido e o código não muda")
    void deveRenomearTipoDeSistema() {
        /* arrange */
        long cliente = idDoTipo("CLIENTE");

        /* act + assert */
        api.requisicao().body(tipo("Cliente Final")).put(TIPOS + "/{id}", cliente)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Cliente Final"))
                .body("codigo", equalTo("CLIENTE"))
                .body("sistema", equalTo(true));
    }

    @Test
    @DisplayName("TIP-16 · DELETE devolve 204 e o tipo some; tipo inexistente devolve 404")
    void deveExcluir() {
        /* arrange */
        long id = api.cadastrarTipo("Entregador");

        /* act + assert */
        api.requisicao().delete(TIPOS + "/{id}", id).then().statusCode(204).body(equalTo(""));
        api.requisicao().get(TIPOS + "/{id}", id).then().statusCode(404);
        api.requisicao().delete(TIPOS + "/{id}", id).then().statusCode(404);
    }

    @Test
    @DisplayName("TIP-11 · DELETE de tipo usado por usuários ativos devolve 409 com a quantidade")
    void deveRecusarTipoEmUso() {
        /* arrange */
        long id = api.cadastrarTipo("Entregador");
        api.cadastrar(usuarioComCpf("ENTREGADOR", "Bruno Lima", "bruno@exemplo.com", "bruno.lima", "52998224725"));
        api.cadastrar(usuarioComCpf("ENTREGADOR", "Carla Dias", "carla@exemplo.com", "carla.dias", "11144477735"));

        /* act + assert */
        api.requisicao().delete(TIPOS + "/{id}", id)
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflito de dados"))
                .body("detail", equalTo("O tipo Entregador não pode ser excluído: 2 usuários ativos o usam."));
    }

    @Test
    @DisplayName("TIP-12 · DELETE de Cliente ou de Dono de Restaurante devolve 409")
    void deveRecusarTipoDeSistema() {
        api.requisicao().delete(TIPOS + "/{id}", idDoTipo("CLIENTE"))
                .then()
                .statusCode(409)
                .body("detail", equalTo("O tipo Cliente é um tipo de sistema e não pode ser excluído."));
        api.requisicao().delete(TIPOS + "/{id}", idDoTipo("DONO_RESTAURANTE")).then().statusCode(409);
    }

    @Test
    @DisplayName("TIP-13 · um tipo que só era usado por usuários removidos pode ser excluído")
    void deveExcluirTipoDeUsuariosRemovidos() {
        /* arrange */
        long tipo = api.cadastrarTipo("Entregador");
        long usuario = api.cadastrar(usuarioComCpf("ENTREGADOR", "Bruno Lima", "bruno@exemplo.com", "bruno.lima",
                "52998224725"));
        api.requisicao().delete("/api/v1/usuarios/{id}", usuario).then().statusCode(204);

        /* act + assert */
        api.requisicao().delete(TIPOS + "/{id}", tipo).then().statusCode(204);
    }

    @Test
    @DisplayName("TIP-17 · GET /{id}/usuarios lista, paginados, só os usuários ativos do tipo; tipo inexistente devolve 404")
    void deveListarUsuariosDoTipo() {
        /* arrange */
        long tipo = api.cadastrarTipo("Entregador");
        api.cadastrar(usuarioComCpf("ENTREGADOR", "Carla Dias", "carla@exemplo.com", "carla.dias", "11144477735"));
        long removido = api.cadastrar(usuarioComCpf("ENTREGADOR", "Bruno Lima", "bruno@exemplo.com", "bruno.lima",
                "52998224725"));
        api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
        api.requisicao().delete("/api/v1/usuarios/{id}", removido).then().statusCode(204);

        /* act + assert */
        api.requisicao().get(TIPOS + "/{id}/usuarios", tipo)
                .then()
                .statusCode(200)
                .body("conteudo.nome", equalTo(List.of("Carla Dias")))
                .body("conteudo[0].tipo", equalTo("ENTREGADOR"))
                .body("totalElementos", equalTo(1));
        api.requisicao().get(TIPOS + "/999999/usuarios").then().statusCode(404);
    }

    private long idDoTipo(String codigo) {
        return jdbc.queryForObject("SELECT id FROM tipo_usuario WHERE codigo = ?", Long.class, codigo);
    }
}
