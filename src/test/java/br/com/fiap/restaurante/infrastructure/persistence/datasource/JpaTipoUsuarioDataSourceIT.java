package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Origem de dados de tipos de usuário contra o MySQL: as restrições do schema e o
 * código que não muda.
 */
@TesteDeIntegracao
@DisplayName("Origem de dados JPA de tipos de usuário")
class JpaTipoUsuarioDataSourceIT {

    @Autowired
    private ITipoUsuarioDataSource tipos;

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

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("TIP-18 · a chave estrangeira recusa excluir um tipo em uso, mesmo sem passar pelo caso de uso")
    void deveBarrarExclusaoDeTipoEmUsoNoBanco() {
        /* arrange */
        DadosTipoUsuario entregador = incluir("Entregador", "ENTREGADOR");
        transacao.executar(() -> usuarios.incluir(new DadosUsuario(null, "Bruno Lima", "bruno@exemplo.com",
                "bruno.lima", "$2a$10$hash", "52998224725", entregador,
                new DadosEndereco("Rua A", "1", null, "Centro", "Itajaí", "SC", "88301000"), null, null)));

        /* act + assert */
        assertThatThrownBy(() -> transacao.executar(() -> tipos.excluir(entregador.id())))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(tipos.buscarPorId(entregador.id())).isPresent();
    }

    @Test
    @DisplayName("TIP-06 · a verificação e a restrição de nome não diferenciam maiúsculas nem acentos")
    void deveCompararNomesSemMaiusculasNemAcentos() {
        /* arrange */
        DadosTipoUsuario gerencia = incluir("Gerência", "GERENCIA");

        /* act + assert */
        assertThat(tipos.existeNome("gerencia")).isTrue();
        assertThat(tipos.existeNome("Entregador")).isFalse();
        assertThat(tipos.existeNomeEmOutroTipo("GERÊNCIA", gerencia.id())).isFalse();
        assertThat(tipos.existeNomeEmOutroTipo("GERÊNCIA", gerencia.id() + 1)).isTrue();
        assertThatThrownBy(() -> incluir("GERENCIA", "GERENCIA_2"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("TIP-04 · atualizar grava só o nome: o código fica como foi criado")
    void deveManterOCodigoNaAtualizacao() {
        /* arrange */
        DadosTipoUsuario entregador = incluir("Entregador", "ENTREGADOR");

        /* act */
        DadosTipoUsuario atualizado = transacao.executar(
                () -> tipos.atualizar(new DadosTipoUsuario(entregador.id(), "Entregador Parceiro", "OUTRO_CODIGO")));

        /* assert */
        assertThat(atualizado).isEqualTo(new DadosTipoUsuario(entregador.id(), "Entregador Parceiro", "ENTREGADOR"));
        assertThat(jdbc.queryForObject("SELECT codigo FROM tipo_usuario WHERE id = ?", String.class, entregador.id()))
                .isEqualTo("ENTREGADOR");
    }

    @Test
    @DisplayName("TIP-16 · a listagem pagina e ordena pelos campos pedidos; excluir remove a linha")
    void deveListarEExcluir() {
        /* arrange */
        DadosTipoUsuario entregador = incluir("Entregador", "ENTREGADOR");

        /* act */
        Pagina<DadosTipoUsuario> pagina = tipos.listar(
                new PedidoDePagina(0, 2, List.of(new PedidoDePagina.Ordem("nome", false))));
        transacao.executar(() -> tipos.excluir(entregador.id()));

        /* assert */
        assertThat(pagina.conteudo()).extracting(DadosTipoUsuario::nome)
                .containsExactly("Entregador", "Dono de Restaurante");
        assertThat(pagina.totalElementos()).isEqualTo(3);
        assertThat(tipos.buscarPorId(entregador.id())).isEmpty();
    }

    private DadosTipoUsuario incluir(String nome, String codigo) {
        return transacao.executar(() -> tipos.incluir(new DadosTipoUsuario(null, nome, codigo)));
    }
}
