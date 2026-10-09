package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.SENHA;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.atualizacao;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.credenciais;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeSenha;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@TesteDeIntegracao
@DisplayName("API de usuários (v1)")
class UsuarioApiIT {

    private static final String USUARIOS = "/api/v1/usuarios";

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

    @Test
    @DisplayName("USU-18 · POST de cliente com CPF devolve 201, Location, tipo CLIENTE e o documento só com dígitos")
    void deveCadastrarCliente() {
        /* act */
        Response resposta = api.requisicao()
                .body(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "123.456.789-09"))
                .post(USUARIOS);

        /* assert */
        resposta.then()
                .statusCode(201)
                .body("nome", equalTo("Maria Silva"))
                .body("tipo", equalTo("CLIENTE"))
                .body("documento", equalTo("12345678909"))
                .body("endereco.estado", equalTo("SC"))
                .body("endereco.cep", equalTo("88301000"))
                .body("$", not(hasKey("senha")));
        assertThat(resposta.header("Location")).endsWith(USUARIOS + "/" + resposta.jsonPath().getLong("id"));
    }

    @Test
    @DisplayName("USU-19 · POST de dono com CNPJ devolve 201 e tipo DONO_RESTAURANTE")
    void deveCadastrarDonoDeRestaurante() {
        api.requisicao()
                .body(dono("João Pereira", "joao@exemplo.com", "joao.pereira", "11.222.333/0001-81"))
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(201)
                .body("tipo", equalTo("DONO_RESTAURANTE"))
                .body("documento", equalTo("11222333000181"));
    }

    @Test
    @DisplayName("USU-20 · POST com e-mail já cadastrado devolve 409")
    void deveRecusarEmailDuplicado() {
        /* arrange */
        api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao()
                .body(cliente("Maria Souza", "maria@exemplo.com", "maria.souza", "52998224725"))
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(409)
                .body("title", equalTo("Conflito de dados"))
                .body("detail", equalTo("O e-mail informado já está cadastrado."));
    }

    @Test
    @DisplayName("USU-21 · POST com Maria@Exemplo.com quando maria@exemplo.com já existe devolve 409")
    void deveRecusarEmailQueSoMudaNasMaiusculas() {
        /* arrange */
        api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao()
                .body(cliente("Maria Souza", "Maria@Exemplo.com", "maria.souza", "52998224725"))
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("USU-22 · POST com campos ausentes ou inválidos devolve 400, com erros nomeando cada campo")
    void deveApontarOsCamposInvalidos() {
        api.requisicao()
                .body("""
                        { "nome": "Ab", "email": "invalido", "login": "x", "senha": "123", "tipo": "CLIENTE" }""")
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(400)
                .body("title", equalTo("Dados inválidos"))
                .body("erros.campo", hasItems("nome", "email", "login", "senha", "endereco"));
    }

    @Test
    @DisplayName("USU-22 · POST com CPF de dígito verificador errado devolve 400 apontando o campo cpf")
    void deveApontarOCpfInvalido() {
        api.requisicao()
                .body(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "111.111.111-11"))
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(400)
                .body("erros.campo", hasItem("cpf"));
    }

    @Test
    @DisplayName("USU-23 · POST de cliente sem CPF devolve 400 com o título \"Regra de negócio violada\"")
    void deveRecusarClienteSemCpf() {
        api.requisicao()
                .body(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909").replace("\"cpf\": \"12345678909\",", ""))
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(400)
                .body("title", equalTo("Regra de negócio violada"))
                .body("detail", equalTo("O CPF é obrigatório para usuários do tipo Cliente."));
    }

    @Test
    @DisplayName("USU-10 · POST com código de tipo inexistente devolve 404")
    void deveRecusarTipoInexistente() {
        api.requisicao()
                .body(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909")
                        .replace("\"CLIENTE\"", "\"ENTREGADOR\""))
        .when()
                .post(USUARIOS)
        .then()
                .statusCode(404)
                .body("detail", equalTo("Tipo de usuário ENTREGADOR não encontrado."));
    }

    @Test
    @DisplayName("USU-24 · GET por id devolve 200; id inexistente devolve 404 com instance igual à rota chamada")
    void deveBuscarPorId() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao().get(USUARIOS + "/{id}", id)
                .then().statusCode(200).body("email", equalTo("maria@exemplo.com"));
        api.requisicao().get(USUARIOS + "/999999")
                .then()
                .statusCode(404)
                .body("instance", equalTo("/api/v1/usuarios/999999"))
                .body("detail", equalTo("Usuário 999999 não encontrado."));
    }

    @Test
    @DisplayName("USU-25 · PUT devolve 200; dataUltimaAlteracao avança e dataCriacao não muda")
    void deveAtualizarMantendoADataDeCriacao() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
        LocalDateTime janeiro = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
        jdbc.update("UPDATE usuario SET data_criacao = ?, data_ultima_alteracao = ? WHERE id = ?", janeiro, janeiro, id);

        /* act */
        Response resposta = api.requisicao()
                .body(atualizacao("Maria Silva Souza", "maria.souza@exemplo.com", "maria.souza"))
                .put(USUARIOS + "/{id}", id);

        /* assert */
        resposta.then()
                .statusCode(200)
                .body("nome", equalTo("Maria Silva Souza"))
                .body("login", equalTo("maria.souza"))
                .body("tipo", equalTo("CLIENTE"))
                .body("documento", equalTo("12345678909"))
                .body("dataCriacao", equalTo("2026-01-10T09:00:00"));
        assertThat(LocalDateTime.parse(resposta.jsonPath().getString("dataUltimaAlteracao"))).isAfter(janeiro);
    }

    @Test
    @DisplayName("USU-12 · PUT com o e-mail de outro usuário devolve 409; USU-14 · PUT de usuário inexistente devolve 404")
    void deveRecusarAtualizacaoInvalida() {
        /* arrange */
        long maria = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
        api.cadastrar(dono("João Pereira", "joao@exemplo.com", "joao.pereira", "11222333000181"));

        /* act + assert */
        api.requisicao().body(atualizacao("Maria Silva", "joao@exemplo.com", "maria.silva"))
                .put(USUARIOS + "/{id}", maria)
                .then().statusCode(409);
        api.requisicao().body(atualizacao("Maria Silva", "maria@exemplo.com", "maria.silva"))
                .put(USUARIOS + "/999999")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("USU-26 · PUT /senha devolve 204; senha atual errada 401; nova senha curta 400; usuário inexistente 404")
    void deveTrocarASenha() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao().body(trocaDeSenha("SenhaErrada999", "SenhaNova456"))
                .put(USUARIOS + "/{id}/senha", id)
                .then()
                .statusCode(401)
                .body("title", equalTo("Credenciais inválidas"))
                .body("detail", equalTo("A senha atual informada está incorreta."));
        api.requisicao().body(trocaDeSenha(SENHA, "123"))
                .put(USUARIOS + "/{id}/senha", id)
                .then().statusCode(400).body("erros.campo", hasItem("novaSenha"));
        api.requisicao().body(trocaDeSenha(SENHA, "SenhaNova456"))
                .put(USUARIOS + "/999999/senha")
                .then().statusCode(404);
        api.requisicao().body(trocaDeSenha(SENHA, "SenhaNova456"))
                .put(USUARIOS + "/{id}/senha", id)
                .then().statusCode(204).body(equalTo(""));
    }

    @Test
    @DisplayName("USU-31 · senha acentuada acima de 72 bytes devolve 400 no cadastro e na troca, e não 500")
    void deveRecusarSenhaAcimaDe72Bytes() {
        /* arrange */
        String acentuada = "é".repeat(37);
        String mensagem = "A senha deve ter no máximo 72 bytes. Letras acentuadas contam como dois.";
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao()
                .body(cliente("João Silva", "joao@exemplo.com", "joao.silva", "52998224725").replace(SENHA, acentuada))
                .post(USUARIOS)
                .then().statusCode(400).body("detail", equalTo(mensagem));
        api.requisicao().body(trocaDeSenha(SENHA, acentuada))
                .put(USUARIOS + "/{id}/senha", id)
                .then().statusCode(400).body("detail", equalTo(mensagem));
    }

    @Test
    @DisplayName("DOC-07 · POST de dono com CNPJ alfanumérico devolve 201; com dígito errado, 400 apontando o campo")
    void deveCadastrarDonoComCnpjAlfanumerico() {
        api.requisicao().body(dono("Ana Costa", "ana@exemplo.com", "ana.costa", "12.ABC.345/01DE-35"))
                .post(USUARIOS)
                .then()
                .statusCode(201)
                .body("tipo", equalTo("DONO_RESTAURANTE"))
                .body("documento", equalTo("12ABC34501DE35"));
        api.requisicao().body(dono("Rui Costa", "rui@exemplo.com", "rui.costa", "12.ABC.345/01DE-36"))
                .post(USUARIOS)
                .then()
                .statusCode(400)
                .body("erros.campo", hasItem("cnpj"));
    }

    @Test
    @DisplayName("USU-27 · GET ?nome= devolve lista simples com resultados, sem resultados e sem filtro")
    void deveBuscarPorNomeEmLista() {
        /* arrange */
        api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
        api.cadastrar(dono("João Pereira", "joao@exemplo.com", "joao.pereira", "11222333000181"));

        /* act + assert */
        api.requisicao().get(USUARIOS + "?nome=MARIA")
                .then().statusCode(200).body("nome", equalTo(List.of("Maria Silva")));
        api.requisicao().get(USUARIOS + "?nome=zzzzzzz")
                .then().statusCode(200).body("$", empty());
        api.requisicao().get(USUARIOS)
                .then().statusCode(200).body("nome", equalTo(List.of("João Pereira", "Maria Silva")));
    }

    @Test
    @DisplayName("USU-29 · nenhuma resposta da API contém a senha, nem o hash dela")
    void naoDeveExporASenha() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act */
        String[] corpos = {
                api.requisicao().get(USUARIOS + "/{id}", id).asString(),
                api.requisicao().get(USUARIOS).asString(),
                api.requisicao().get("/api/v2/usuarios").asString(),
                api.requisicao().body(atualizacao("Maria Silva", "maria@exemplo.com", "maria.silva"))
                        .put(USUARIOS + "/{id}", id).asString(),
                api.requisicao().body(credenciais("maria.silva", SENHA)).post("/api/v1/auth/login").asString()
        };

        /* assert */
        assertThat(corpos).allSatisfy(corpo -> assertThat(corpo)
                .contains("Maria Silva")
                .doesNotContain("\"senha\"")
                .doesNotContain("$2a$"));
    }

    @Test
    @DisplayName("EXC-02 · DELETE devolve 204; um segundo DELETE, ou de id inexistente, devolve 404")
    void deveExcluirUmaVezSo() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao().delete(USUARIOS + "/{id}", id).then().statusCode(204).body(equalTo(""));
        api.requisicao().delete(USUARIOS + "/{id}", id).then().statusCode(404);
        api.requisicao().delete(USUARIOS + "/999999").then().statusCode(404);
    }

    @Test
    @DisplayName("EXC-03 · depois da exclusão, o GET devolve 404, a busca não traz o usuário e o login falha")
    void deveSumirDepoisDeExcluido() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act */
        api.requisicao().delete(USUARIOS + "/{id}", id).then().statusCode(204);

        /* assert */
        api.requisicao().get(USUARIOS + "/{id}", id).then().statusCode(404);
        api.requisicao().get(USUARIOS + "?nome=maria").then().statusCode(200).body("$", empty());
        api.requisicao().body(credenciais("maria.silva", SENHA)).post("/api/v1/auth/login").then().statusCode(401);
    }

    @Test
    @DisplayName("EXC-05 · depois da exclusão, o mesmo e-mail, login e documento podem ser cadastrados de novo, com id novo")
    void devePermitirRecadastroDepoisDaExclusao() {
        /* arrange */
        long antigo = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));
        api.requisicao().delete(USUARIOS + "/{id}", antigo).then().statusCode(204);

        /* act */
        long novo = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* assert */
        assertThat(novo).isNotEqualTo(antigo);
        api.requisicao().get(USUARIOS).then().body("id", hasSize(1));
    }

    @Test
    @DisplayName("TOK-03 · nenhum endpoint exige token: requisições sem Authorization, ou com um token qualquer, são atendidas")
    void naoDeveExigirToken() {
        /* arrange */
        long id = api.cadastrar(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909"));

        /* act + assert */
        api.requisicao().get(USUARIOS + "/{id}", id).then().statusCode(200);
        api.requisicao().header("Authorization", "Bearer token-qualquer")
                .get(USUARIOS + "/{id}", id)
                .then().statusCode(200).body("id", equalTo((int) id));
    }
}
