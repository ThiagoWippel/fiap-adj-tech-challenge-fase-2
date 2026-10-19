package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.RestauranteApplication;
import br.com.fiap.restaurante.suporte.ApiDeTeste;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.mysql.MySQLContainer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.credenciais;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Migrações do Flyway com a aplicação inteira subindo, como no compose. Cada cenário tem um banco
 * próprio, num MySQL só desta classe: um banco vazio, ou um criado pelo script da Fase 1 do jeito
 * que o compose da Fase 1 o deixava, com usuários e sem o histórico do Flyway.
 */
@DisplayName("Migrações do banco")
class MigracoesDoBancoIT {

    private static final String SENHA_DA_FASE_1 = "SenhaDaFase1";
    private static final String HASH_DA_FASE_1 = new BCryptPasswordEncoder().encode(SENHA_DA_FASE_1);

    private static final List<String> MIGRACOES_DA_FASE_2 = List.of(
            "V2 tipos de usuario SQL sucesso",
            "V3 usuario da fase 2 SQL sucesso",
            "V4 restaurantes e turnos SQL sucesso",
            "V5 itens do cardapio SQL sucesso");

    // Como root, para criar um banco por cenário
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4").withUsername("root");

    @BeforeAll
    static void subirMysql() {
        MYSQL.start();
    }

    @AfterAll
    static void derrubarMysql() {
        MYSQL.stop();
    }

    @Test
    @DisplayName("INF-11 · num banco vazio, a aplicação aplica as migrações V1 a V5, em ordem, e todas ficam registradas como sucesso")
    void deveAplicarAsCincoMigracoesEmOrdem_QuandoOBancoEstiverVazio() {
        /* arrange */
        JdbcTemplate banco = criarBanco("vazio");

        /* act */
        try (var aplicacao = subirAplicacao("vazio")) {

            /* assert */
            assertThat(historico(banco)).containsExactlyElementsOf(comAFase2("V1 estrutura da fase 1 SQL sucesso"));
        }
    }

    @Test
    @DisplayName("INF-12 · o banco da Fase 1 é reconhecido como V1, recebe da V2 à V5, e os usuários chegam convertidos, com o mesmo id, login, senha e datas")
    void deveAtualizarOBancoDaFase1_SemPerderOsUsuarios() {
        /* arrange */
        JdbcTemplate banco = bancoDaFase1("fase1_conversao");

        /* act */
        try (var aplicacao = subirAplicacao("fase1_conversao")) {

            /* assert */
            assertThat(historico(banco)).containsExactlyElementsOf(comAFase2("V1 << Flyway Baseline >> BASELINE sucesso"));
            assertThat(banco.queryForList("""
                    SELECT CONCAT_WS(' | ', u.id, u.nome, u.email, u.login, t.codigo, u.documento, u.endereco_cep,
                                     u.endereco_rua, u.endereco_cidade, u.data_criacao, u.data_ultima_alteracao)
                    FROM usuario u JOIN tipo_usuario t ON t.id = u.tipo_usuario_id
                    ORDER BY u.id""", String.class))
                    .containsExactly(
                            "41 | João da Silva | joao@fase1.com | joao.silva | CLIENTE | 12345678909 | 01304001"
                                    + " | Rua da Consolação | São Paulo | 2025-08-10 09:15:00.000000 | 2025-09-01 18:40:00.000000",
                            "42 | Ana Ribeiro | ana@fase1.com | ana.ribeiro | DONO_RESTAURANTE | 11222333000181 | 88301000"
                                    + " | Rua das Flores | Itajaí | 2025-08-12 14:00:00.000000 | 2025-08-12 14:00:00.000000");
            assertThat(banco.queryForList("SELECT senha FROM usuario", String.class)).containsOnly(HASH_DA_FASE_1);

            // A Fase 2 lê o registro convertido pelo mapeamento dela
            new ApiDeTeste(porta(aplicacao)).requisicao()
                    .get("/api/v1/usuarios/{id}", 42)
                    .then().statusCode(200)
                    .body("tipo", equalTo("DONO_RESTAURANTE"))
                    .body("documento", equalTo("11222333000181"))
                    .body("email", equalTo("ana@fase1.com"));
        }
    }

    @Test
    @DisplayName("INF-13 · o cliente da Fase 1 faz login na Fase 2 com a senha de antes, e outra senha recebe 401")
    void deveAutenticarOClienteDaFase1_ComASenhaDeAntes() {
        /* arrange */
        bancoDaFase1("fase1_login");

        try (var aplicacao = subirAplicacao("fase1_login")) {
            ApiDeTeste api = new ApiDeTeste(porta(aplicacao));

            /* act + assert */
            api.requisicao().body(credenciais("joao.silva", SENHA_DA_FASE_1))
                    .post("/api/v1/auth/login")
                    .then().statusCode(200)
                    .body("id", equalTo(41))
                    .body("tipo", equalTo("CLIENTE"))
                    .body("token", notNullValue());
            api.requisicao().body(credenciais("joao.silva", "OutraSenha123"))
                    .post("/api/v1/auth/login")
                    .then().statusCode(401);
        }
    }

    @Test
    @DisplayName("INF-14 · num banco já na versão atual, subir a aplicação de novo não reaplica nenhuma migração")
    void naoDeveReaplicarMigracoes_QuandoOBancoJaEstiverAtualizado() {
        /* arrange */
        JdbcTemplate banco = criarBanco("atualizado");
        subirAplicacao("atualizado").close();
        List<String> antes = historicoCompleto(banco);

        /* act */
        try (var aplicacao = subirAplicacao("atualizado")) {

            /* assert */
            assertThat(historicoCompleto(banco)).hasSize(5).isEqualTo(antes);
            new ApiDeTeste(porta(aplicacao)).requisicao()
                    .get("/actuator/health")
                    .then().statusCode(200)
                    .body("status", equalTo("UP"));
        }
    }

    /** O banco como a Fase 1 o deixava: o script dela, um cliente e um dono, sem o histórico do Flyway. */
    private static JdbcTemplate bancoDaFase1(String nome) {
        JdbcTemplate banco = criarBanco(nome);
        banco.execute((ConnectionCallback<Void>) conexao -> {
            ScriptUtils.executeSqlScript(conexao,
                    new EncodedResource(new ClassPathResource("fase-1/01-schema.sql"), StandardCharsets.UTF_8));
            return null;
        });
        // A Fase 1 aceitava o CEP com hífen e não aparava o e-mail
        banco.update("""
                INSERT INTO usuario (id, tipo_usuario, nome, email, login, senha, cpf, cnpj,
                                     data_criacao, data_ultima_alteracao, endereco_rua, endereco_numero,
                                     endereco_complemento, endereco_bairro, endereco_cidade, endereco_estado,
                                     endereco_cep)
                VALUES (41, 'CLIENTE', 'João da Silva', 'joao@fase1.com', 'joao.silva', ?, '12345678909', NULL,
                        '2025-08-10 09:15:00', '2025-09-01 18:40:00', 'Rua da Consolação', '100',
                        NULL, 'Consolação', 'São Paulo', 'SP', '01304-001'),
                       (42, 'DONO_RESTAURANTE', 'Ana Ribeiro', ' ana@fase1.com ', 'ana.ribeiro', ?, NULL,
                        '11222333000181', '2025-08-12 14:00:00', '2025-08-12 14:00:00', 'Rua das Flores', '123',
                        'Sala 2', 'Centro', 'Itajaí', 'SC', '88301000')""", HASH_DA_FASE_1, HASH_DA_FASE_1);
        return banco;
    }

    private static JdbcTemplate criarBanco(String nome) {
        new JdbcTemplate(fonte(MYSQL.getDatabaseName())).execute("CREATE DATABASE " + nome);
        return new JdbcTemplate(fonte(nome));
    }

    /** A aplicação inteira, com o perfil dos testes, numa porta livre e apontando para o banco do cenário. */
    private static ConfigurableApplicationContext subirAplicacao(String banco) {
        return new SpringApplicationBuilder(RestauranteApplication.class).run(
                "--spring.profiles.active=test",
                "--server.port=0",
                "--spring.datasource.url=" + url(banco),
                "--spring.datasource.username=" + MYSQL.getUsername(),
                "--spring.datasource.password=" + MYSQL.getPassword());
    }

    private static List<String> comAFase2(String primeira) {
        return Stream.concat(Stream.of(primeira), MIGRACOES_DA_FASE_2.stream()).toList();
    }

    private static List<String> historico(JdbcTemplate banco) {
        return banco.queryForList("""
                SELECT CONCAT('V', version, ' ', description, ' ', type, IF(success, ' sucesso', ' falha'))
                FROM flyway_schema_history ORDER BY installed_rank""", String.class);
    }

    /** Com a soma de verificação e a hora da aplicação: uma migração reaplicada mudaria a lista. */
    private static List<String> historicoCompleto(JdbcTemplate banco) {
        return banco.queryForList("""
                SELECT CONCAT_WS(' ', installed_rank, version, checksum, installed_on, success)
                FROM flyway_schema_history ORDER BY installed_rank""", String.class);
    }

    private static int porta(ConfigurableApplicationContext aplicacao) {
        return aplicacao.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
    }

    private static DriverManagerDataSource fonte(String banco) {
        return new DriverManagerDataSource(url(banco), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private static String url(String banco) {
        return "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
                .formatted(MYSQL.getHost(), MYSQL.getMappedPort(3306), banco);
    }
}
