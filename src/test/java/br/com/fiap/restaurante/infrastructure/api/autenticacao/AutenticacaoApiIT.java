package br.com.fiap.restaurante.infrastructure.api.autenticacao;

import br.com.fiap.restaurante.infrastructure.config.TokenProperties;
import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.SENHA;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.credenciais;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeSenha;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@TesteDeIntegracao
@DisplayName("API de autenticação")
class AutenticacaoApiIT {

    private static final String LOGIN = "/api/v1/auth/login";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TokenProperties token;

    private ApiDeTeste api;
    private long idMaria;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        idMaria = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
    }

    @Test
    @DisplayName("LOG-08 · login com espaços nas pontas autentica, como o cadastro, que guarda o login sem eles")
    void deveAutenticarComEspacosNasPontas() {
        api.requisicao().body(credenciais(" maria.silva ", SENHA)).post(LOGIN)
                .then().statusCode(200).body("id", equalTo((int) idMaria));
    }

    @Test
    @DisplayName("LOG-01 · credenciais válidas devolvem id, nome, tipo, token e validade, sem dados de perfil")
    void deveAutenticar() {
        api.requisicao()
                .body(credenciais("maria.silva", SENHA))
        .when()
                .post(LOGIN)
        .then()
                .statusCode(200)
                .body("id", equalTo((int) idMaria))
                .body("nome", equalTo("Maria Silva"))
                .body("tipo", equalTo("CLIENTE"))
                .body("token", notNullValue())
                .body("expiraEm", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
                .body("$", not(hasKey("email")))
                .body("$", not(hasKey("endereco")))
                .body("$", not(hasKey("documento")));
    }

    @Test
    @DisplayName("TOK-01 · o token devolvido é assinado com a chave configurada e traz o id do usuário no sub")
    void deveDevolverTokenAssinado() throws Exception {
        /* act */
        String valor = api.requisicao().body(credenciais("maria.silva", SENHA)).post(LOGIN).jsonPath().getString("token");

        /* assert */
        SignedJWT jwt = SignedJWT.parse(valor);
        assertThat(jwt.verify(new MACVerifier(token.chave().getBytes(StandardCharsets.UTF_8)))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(String.valueOf(idMaria));
        assertThat(jwt.getJWTClaimsSet().getStringClaim("tipo")).isEqualTo("CLIENTE");
    }

    @Test
    @DisplayName("LOG-02 · login inexistente devolve 401 com a mesma mensagem da senha incorreta")
    void deveResponderIgualParaLoginInexistenteESenhaErrada() {
        /* act */
        Response loginInexistente = api.requisicao().body(credenciais("ninguem.aqui", SENHA)).post(LOGIN);
        Response senhaErrada = api.requisicao().body(credenciais("maria.silva", "SenhaErrada999")).post(LOGIN);

        /* assert */
        loginInexistente.then().statusCode(401).body("title", equalTo("Credenciais inválidas"));
        senhaErrada.then().statusCode(401);
        String detalhe = loginInexistente.jsonPath().getString("detail");
        assertThat(detalhe).isEqualTo(senhaErrada.jsonPath().getString("detail"));
        assertThat(detalhe.toLowerCase()).doesNotContain("não encontrado").doesNotContain("inexistente");
    }

    @Test
    @DisplayName("LOG-04 · senha incorreta devolve 401")
    void deveRecusarSenhaIncorreta() {
        api.requisicao()
                .body(credenciais("maria.silva", "SenhaErrada999"))
        .when()
                .post(LOGIN)
        .then()
                .statusCode(401)
                .body("detail", equalTo("Login ou senha inválidos."));
    }

    @Test
    @DisplayName("LOG-05 · depois da troca, a senha antiga deixa de funcionar e a nova passa a valer")
    void deveAceitarSoASenhaNovaDepoisDaTroca() {
        /* arrange */
        api.requisicao().body(trocaDeSenha(SENHA, "SenhaNova456"))
                .put("/api/v1/usuarios/{id}/senha", idMaria)
                .then().statusCode(204);

        /* act + assert */
        api.requisicao().body(credenciais("maria.silva", SENHA)).post(LOGIN).then().statusCode(401);
        api.requisicao().body(credenciais("maria.silva", "SenhaNova456")).post(LOGIN).then().statusCode(200);
    }

    @Test
    @DisplayName("LOG-07 · login ou senha ausente devolve 400, apontando os campos")
    void deveRecusarCorpoIncompleto() {
        api.requisicao()
                .body("""
                        { "login": "" }""")
        .when()
                .post(LOGIN)
        .then()
                .statusCode(400)
                .body("erros.campo", hasItems("login", "senha"));
    }
}
