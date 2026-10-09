package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Gateway de tipos de usuário")
class TipoUsuarioGatewayTest {

    @Mock
    private ITipoUsuarioDataSource dataSource;

    private AutoCloseable mocks;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-05 · a busca por código devolve o tipo convertido, ou vazio")
    void deveBuscarPorCodigo() {
        /* arrange */
        when(dataSource.buscarPorCodigo("CLIENTE")).thenReturn(Optional.of(new DadosTipoUsuario(1L, "Cliente", "CLIENTE")));
        TipoUsuarioGateway gateway = TipoUsuarioGateway.create(dataSource);

        /* act */
        Optional<TipoUsuario> cliente = gateway.buscarPorCodigo("CLIENTE");

        /* assert */
        assertThat(cliente).get().extracting(TipoUsuario::getId, TipoUsuario::getNome, TipoUsuario::getCodigo)
                .containsExactly(1L, "Cliente", "CLIENTE");
        assertThat(gateway.buscarPorCodigo("ENTREGADOR")).isEmpty();
    }

    @Test
    @DisplayName("TIP-16 · incluir, atualizar, buscar por id e listar convertem entre dados e entidade")
    void deveConverterNasDemaisOperacoes() {
        /* arrange */
        DadosTipoUsuario entregador = new DadosTipoUsuario(3L, "Entregador", "ENTREGADOR");
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());
        when(dataSource.incluir(new DadosTipoUsuario(null, "Entregador", "ENTREGADOR"))).thenReturn(entregador);
        when(dataSource.atualizar(entregador)).thenReturn(entregador);
        when(dataSource.buscarPorId(3L)).thenReturn(Optional.of(entregador));
        when(dataSource.listar(pedido)).thenReturn(new Pagina<>(List.of(entregador), 0, 10, 1, 1));
        TipoUsuarioGateway gateway = TipoUsuarioGateway.create(dataSource);

        /* act */
        TipoUsuario incluido = gateway.incluir(TipoUsuario.create("Entregador"));
        TipoUsuario atualizado = gateway.atualizar(TipoUsuario.create(3L, "Entregador", "ENTREGADOR"));
        Optional<TipoUsuario> encontrado = gateway.buscarPorId(3L);
        Pagina<TipoUsuario> pagina = gateway.listar(pedido);

        /* assert */
        assertThat(incluido.getId()).isEqualTo(3L);
        assertThat(atualizado.getCodigo()).isEqualTo("ENTREGADOR");
        assertThat(encontrado).get().extracting(TipoUsuario::getNome).isEqualTo("Entregador");
        assertThat(gateway.buscarPorId(99L)).isEmpty();
        assertThat(pagina.conteudo()).extracting(TipoUsuario::getCodigo).containsExactly("ENTREGADOR");
    }

    @Test
    @DisplayName("TIP-06 · as verificações de nome e a exclusão são repassadas à origem de dados")
    void deveRepassarVerificacoesEExclusao() {
        /* arrange */
        when(dataSource.existeNome("Entregador")).thenReturn(true);
        when(dataSource.existeNomeEmOutroTipo("Entregador", 4L)).thenReturn(true);
        TipoUsuarioGateway gateway = TipoUsuarioGateway.create(dataSource);

        /* act */
        gateway.excluir(3L);

        /* assert */
        assertThat(gateway.existeNome("Entregador")).isTrue();
        assertThat(gateway.existeNomeEmOutroTipo("Entregador", 4L)).isTrue();
        verify(dataSource).excluir(3L);
    }

    @Test
    @DisplayName("CON-03 · a busca para alterar devolve o tipo convertido, ou vazio")
    void deveBuscarParaAlterar() {
        /* arrange */
        when(dataSource.buscarPorIdParaAlterar(3L))
                .thenReturn(Optional.of(new DadosTipoUsuario(3L, "Entregador", "ENTREGADOR")));
        TipoUsuarioGateway gateway = TipoUsuarioGateway.create(dataSource);

        /* act + assert */
        assertThat(gateway.buscarPorIdParaAlterar(3L)).get().extracting(TipoUsuario::getCodigo)
                .isEqualTo("ENTREGADOR");
        assertThat(gateway.buscarPorIdParaAlterar(99L)).isEmpty();
    }
}
