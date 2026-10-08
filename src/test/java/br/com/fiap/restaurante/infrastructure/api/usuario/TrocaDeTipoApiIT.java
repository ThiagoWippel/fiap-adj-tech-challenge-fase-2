package br.com.fiap.restaurante.infrastructure.api.usuario;

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

import static br.com.fiap.restaurante.suporte.ApiDeTeste.SENHA;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.credenciais;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeTipo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@TesteDeIntegracao
@DisplayName("API de troca de tipo do usuário")
class TrocaDeTipoApiIT {

    private static final String TIPO_DA_MARIA = "/api/v1/usuarios/{id}/tipo";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private long maria;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        maria = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("TRO-08 · PATCH devolve 200 com o novo tipo e o novo documento, na mesma conta")
    void deveTrocarOTipo() {
        /* arrange */
        LocalDateTime janeiro = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
        jdbc.update("UPDATE usuario SET data_criacao = ?, data_ultima_alteracao = ? WHERE id = ?", janeiro, janeiro, maria);

        /* act */
        Response resposta = api.requisicao().body(trocaDeTipo("DONO_RESTAURANTE", "11.222.333/0001-81"))
                .patch(TIPO_DA_MARIA, maria);

        /* assert */
        resposta.then()
                .statusCode(200)
                .body("id", equalTo((int) maria))
                .body("tipo", equalTo("DONO_RESTAURANTE"))
                .body("documento", equalTo("11222333000181"))
                .body("email", equalTo("maria@exemplo.com"))
                .body("dataCriacao", equalTo("2026-01-10T09:00:00"));
        assertThat(LocalDateTime.parse(resposta.jsonPath().getString("dataUltimaAlteracao"))).isAfter(janeiro);
        api.requisicao().body(credenciais("maria.silva", SENHA)).post("/api/v1/auth/login")
                .then().statusCode(200).body("tipo", equalTo("DONO_RESTAURANTE"));
    }

    @Test
    @DisplayName("TRO-08 · documento incompatível com o tipo, ou ausente, devolve 400")
    void deveRecusarDocumentoIncompativel() {
        api.requisicao().body(trocaDeTipo("DONO_RESTAURANTE", "52998224725")).patch(TIPO_DA_MARIA, maria)
                .then()
                .statusCode(400)
                .body("title", equalTo("Regra de negócio violada"))
                .body("detail", equalTo("Usuário do tipo Dono de Restaurante deve informar CNPJ."));
        api.requisicao().body("""
                        { "tipo": "DONO_RESTAURANTE" }""").patch(TIPO_DA_MARIA, maria)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItem("documento"));
    }

    @Test
    @DisplayName("TRO-08 · usuário ou tipo inexistente devolve 404")
    void deveRecusarUsuarioOuTipoInexistente() {
        api.requisicao().body(trocaDeTipo("DONO_RESTAURANTE", "11222333000181")).patch(TIPO_DA_MARIA, 999999)
                .then().statusCode(404).body("detail", equalTo("Usuário 999999 não encontrado."));
        api.requisicao().body(trocaDeTipo("ENTREGADOR", "12345678909")).patch(TIPO_DA_MARIA, maria)
                .then().statusCode(404).body("detail", equalTo("Tipo de usuário ENTREGADOR não encontrado."));
    }

    @Test
    @DisplayName("TRO-08 · documento usado por outro usuário devolve 409")
    void deveRecusarDocumentoEmUso() {
        /* arrange */
        api.cadastrar(dono("João Pereira", "joao@exemplo.com", "joao.pereira", "11222333000181"));

        /* act + assert */
        api.requisicao().body(trocaDeTipo("DONO_RESTAURANTE", "11222333000181")).patch(TIPO_DA_MARIA, maria)
                .then()
                .statusCode(409)
                .body("detail", equalTo("O documento informado já está cadastrado."));
    }

    @Test
    @DisplayName("TRO-07 · trocar para o tipo atual com o mesmo documento devolve 200 sem alterar nada")
    void deveAceitarTrocaSemMudanca() {
        /* arrange */
        LocalDateTime janeiro = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
        jdbc.update("UPDATE usuario SET data_criacao = ?, data_ultima_alteracao = ? WHERE id = ?", janeiro, janeiro, maria);

        /* act + assert */
        api.requisicao().body(trocaDeTipo("CLIENTE", "123.456.789-09")).patch(TIPO_DA_MARIA, maria)
                .then()
                .statusCode(200)
                .body("tipo", equalTo("CLIENTE"))
                .body("dataUltimaAlteracao", equalTo("2026-01-10T09:00:00"));
    }

    @Test
    @DisplayName("TRO-09 · depois da troca, o GET do usuário e a lista de usuários do tipo mostram o novo tipo")
    void deveRefletirOTipoNovoNasConsultas() {
        /* arrange */
        long cliente = jdbc.queryForObject("SELECT id FROM tipo_usuario WHERE codigo = 'CLIENTE'", Long.class);
        long dono = jdbc.queryForObject("SELECT id FROM tipo_usuario WHERE codigo = 'DONO_RESTAURANTE'", Long.class);

        /* act */
        api.requisicao().body(trocaDeTipo("DONO_RESTAURANTE", "11222333000181")).patch(TIPO_DA_MARIA, maria)
                .then().statusCode(200);

        /* assert */
        api.requisicao().get("/api/v1/usuarios/{id}", maria).then().body("tipo", equalTo("DONO_RESTAURANTE"));
        api.requisicao().get("/api/v1/tipos-usuario/{id}/usuarios", dono)
                .then().body("conteudo.nome", equalTo(List.of("Maria Silva")));
        api.requisicao().get("/api/v1/tipos-usuario/{id}/usuarios", cliente).then().body("conteudo", empty());
    }
}
