package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Origem de dados de usuários contra o MySQL: auditoria, anonimização e as
 * restrições do schema.
 */
@TesteDeIntegracao
@DisplayName("Origem de dados JPA de usuários")
class JpaUsuarioDataSourceIT {

    private static final DadosTipoUsuario CLIENTE = new DadosTipoUsuario(1L, "Cliente", "CLIENTE");
    private static final DadosEndereco ENDERECO =
            new DadosEndereco("Rua das Flores", "123", null, "Centro", "Itajaí", "SC", "88301000");
    private static final String HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private static final List<String> COLUNAS = List.of("nome", "email", "login", "senha", "documento",
            "tipo_usuario_id", "endereco_rua", "endereco_numero", "endereco_bairro", "endereco_cidade",
            "endereco_estado", "endereco_cep");

    @Autowired
    private IUsuarioDataSource usuarios;

    @Autowired
    private ITransactionManager transacao;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("USU-30 · atualizar mantém dataCriacao e avança dataUltimaAlteracao (a armadilha do update)")
    void deveManterADataDeCriacaoNaAtualizacao() {
        /* arrange */
        Long id = transacao.executar(() -> usuarios.incluir(maria("maria@exemplo.com", "maria.silva", "12345678909"))).id();
        DadosUsuario antes = usuarios.buscarPorId(id).orElseThrow();
        DadosUsuario alterado = new DadosUsuario(id, "Maria Silva Souza", antes.email(), antes.login(), antes.senha(),
                antes.documento(), antes.tipo(), antes.endereco(), null, null);

        /* act */
        DadosUsuario atualizado = transacao.executar(() -> usuarios.atualizar(alterado));

        /* assert */
        DadosUsuario depois = usuarios.buscarPorId(id).orElseThrow();
        assertThat(depois.nome()).isEqualTo("Maria Silva Souza");
        assertThat(atualizado.dataCriacao()).isEqualTo(antes.dataCriacao());
        assertThat(depois.dataCriacao()).isEqualTo(antes.dataCriacao());
        assertThat(depois.dataUltimaAlteracao()).isAfter(antes.dataUltimaAlteracao());
    }

    @Test
    @DisplayName("EXC-04 · a anonimização mantém id e datas, troca o nome e apaga os dados pessoais")
    void deveAnonimizarOUsuario() {
        /* arrange */
        Long id = transacao.executar(() -> usuarios.incluir(maria("maria@exemplo.com", "maria.silva", "12345678909"))).id();
        Object criadoEm = jdbc.queryForObject("SELECT data_criacao FROM usuario WHERE id = ?", Object.class, id);

        /* act */
        transacao.executar(() -> usuarios.anonimizar(id));

        /* assert */
        Map<String, Object> linha = jdbc.queryForMap("SELECT * FROM usuario WHERE id = ?", id);
        assertThat(linha.get("nome")).isEqualTo("Usuário removido");
        assertThat(linha.get("data_criacao")).isEqualTo(criadoEm);
        assertThat(linha.get("data_ultima_alteracao")).isNotNull();
        assertThat(linha.get("removido_em")).isNotNull();
        assertThat(COLUNAS.subList(1, COLUNAS.size())).allSatisfy(coluna -> assertThat(linha.get(coluna)).isNull());
        assertThat(usuarios.buscarPorId(id)).isEmpty();
        assertThat(usuarios.buscarPorNome("")).isEmpty();
    }

    @ParameterizedTest(name = "EXC-06 · o banco recusa usuário ativo com {0} nulo")
    @ValueSource(strings = {"email", "login", "senha", "documento", "tipo_usuario_id", "endereco_rua",
            "endereco_numero", "endereco_bairro", "endereco_cidade", "endereco_estado", "endereco_cep"})
    void deveRecusarUsuarioAtivoIncompleto(String colunaNula) {
        /* arrange */
        List<Object> valores = new ArrayList<>(Arrays.asList("Maria Silva", "maria@exemplo.com", "maria.silva",
                HASH, "12345678909", 1L, "Rua das Flores", "123", "Centro", "Itajaí", "SC", "88301000"));
        valores.set(COLUNAS.indexOf(colunaNula), null);
        String sql = "INSERT INTO usuario (" + String.join(", ", COLUNAS)
                + ", data_criacao, data_ultima_alteracao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(6), NOW(6))";

        /* act + assert */
        assertThatThrownBy(() -> jdbc.update(sql, valores.toArray()))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("ck_usuario_ativo_completo");
    }

    @Test
    @DisplayName("USU-21 · a restrição única do banco não diferencia maiúsculas e barra o e-mail repetido mesmo sem o caso de uso")
    void deveBarrarEmailRepetidoNoBanco() {
        /* arrange */
        transacao.executar(() -> usuarios.incluir(maria("maria@exemplo.com", "maria.silva", "12345678909")));

        /* act + assert */
        assertThat(usuarios.existeEmail("MARIA@EXEMPLO.COM")).isTrue();
        assertThatThrownBy(() -> transacao.executar(
                () -> usuarios.incluir(maria("Maria@Exemplo.com", "maria.souza", "52998224725"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("USU-13 · as verificações de unicidade ignoram o próprio usuário")
    void deveIgnorarOProprioUsuarioNasVerificacoes() {
        /* arrange */
        Long id = transacao.executar(() -> usuarios.incluir(maria("maria@exemplo.com", "maria.silva", "12345678909"))).id();

        /* act + assert */
        assertThat(usuarios.existeEmailEmOutroUsuario("maria@exemplo.com", id)).isFalse();
        assertThat(usuarios.existeEmailEmOutroUsuario("maria@exemplo.com", id + 1)).isTrue();
        assertThat(usuarios.existeLoginEmOutroUsuario("maria.silva", id)).isFalse();
        assertThat(usuarios.existeLoginEmOutroUsuario("maria.silva", id + 1)).isTrue();
        assertThat(usuarios.existeLogin("Maria.Silva")).isTrue();
        assertThat(usuarios.existeDocumento("12345678909")).isTrue();
        assertThat(usuarios.existeDocumento("52998224725")).isFalse();
    }

    @Test
    @DisplayName("TIP-11 · TIP-17 · contagem e busca por tipo consideram só os usuários ativos")
    void deveContarEBuscarSoUsuariosAtivosDoTipo() {
        /* arrange */
        transacao.executar(() -> usuarios.incluir(maria("maria@exemplo.com", "maria.silva", "12345678909")));
        Long removida = transacao.executar(
                () -> usuarios.incluir(maria("maria2@exemplo.com", "maria.souza", "52998224725"))).id();
        transacao.executar(() -> usuarios.anonimizar(removida));
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));

        /* act + assert */
        assertThat(usuarios.contarAtivosPorTipo(CLIENTE.id())).isEqualTo(1);
        assertThat(usuarios.buscarPorTipo(CLIENTE.id(), pedido).conteudo())
                .extracting(DadosUsuario::email).containsExactly("maria@exemplo.com");
        assertThat(usuarios.contarAtivosPorTipo(2L)).isZero();
    }

    @Test
    @DisplayName("TRO-04 · a verificação de documento em outro usuário ignora o próprio usuário")
    void deveVerificarDocumentoEmOutroUsuario() {
        /* arrange */
        Long id = transacao.executar(() -> usuarios.incluir(maria("maria@exemplo.com", "maria.silva", "12345678909"))).id();

        /* act + assert */
        assertThat(usuarios.existeDocumentoEmOutroUsuario("12345678909", id)).isFalse();
        assertThat(usuarios.existeDocumentoEmOutroUsuario("12345678909", id + 1)).isTrue();
    }

    private static DadosUsuario maria(String email, String login, String documento) {
        return new DadosUsuario(null, "Maria Silva", email, login, HASH, documento, CLIENTE, ENDERECO, null, null);
    }
}
