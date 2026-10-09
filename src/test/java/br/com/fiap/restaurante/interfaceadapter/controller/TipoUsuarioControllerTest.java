package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.dto.RenomeacaoDeTipoUsuarioDTO;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.TipoUsuarioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Controller de tipos de usuário")
class TipoUsuarioControllerTest {

    private static final DadosTipoUsuario ENTREGADOR = new DadosTipoUsuario(3L, "Entregador", "ENTREGADOR");
    private static final PedidoDePagina PEDIDO = new PedidoDePagina(0, 10, List.of());

    @Mock
    private ITipoUsuarioDataSource tipos;

    @Mock
    private IUsuarioDataSource usuarios;

    private AutoCloseable mocks;
    private TipoUsuarioController controller;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = TipoUsuarioController.create(tipos, usuarios, new TransacaoImediata());
        when(tipos.buscarPorId(3L)).thenReturn(Optional.of(ENTREGADOR));
        when(tipos.buscarPorIdParaAlterar(3L)).thenReturn(Optional.of(ENTREGADOR));
        when(tipos.buscarPorId(1L)).thenReturn(Optional.of(DadosDeExemplo.CLIENTE));
        when(tipos.buscarPorIdParaAlterar(1L)).thenReturn(Optional.of(DadosDeExemplo.CLIENTE));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-16 · cadastrar devolve o tipo com o id e o código gerado")
    void deveCadastrar() {
        /* arrange */
        when(tipos.buscarPorCodigo("ENTREGADOR")).thenReturn(Optional.empty());
        when(tipos.incluir(any())).thenReturn(ENTREGADOR);

        /* act */
        TipoUsuarioResponse resposta = controller.cadastrar("Entregador");

        /* assert */
        assertThat(resposta).isEqualTo(new TipoUsuarioResponse(3L, "Entregador", "ENTREGADOR", false));
    }

    @Test
    @DisplayName("TIP-16 · buscar por id, listar e renomear devolvem as respostas")
    void deveConsultarERenomear() {
        /* arrange */
        when(tipos.listar(PEDIDO)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.CLIENTE, ENTREGADOR), 0, 10, 2, 1));
        when(tipos.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        TipoUsuarioResponse porId = controller.buscarPorId(1L);
        PaginaResponse<TipoUsuarioResponse> pagina = controller.listar(PEDIDO);
        TipoUsuarioResponse renomeado = controller.renomear(new RenomeacaoDeTipoUsuarioDTO(3L, "Entregador Parceiro"));

        /* assert */
        assertThat(porId.sistema()).isTrue();
        assertThat(pagina.conteudo()).hasSize(2);
        assertThat(renomeado.nome()).isEqualTo("Entregador Parceiro");
        assertThat(renomeado.codigo()).isEqualTo("ENTREGADOR");
    }

    @Test
    @DisplayName("TIP-10 · excluir remove o tipo sem usuários ativos")
    void deveExcluir() {
        /* act */
        controller.excluir(3L);

        /* assert */
        verify(tipos).excluir(3L);
    }

    @Test
    @DisplayName("TIP-17 · lista os usuários do tipo")
    void deveListarUsuariosDoTipo() {
        /* arrange */
        when(usuarios.buscarPorTipo(1L, PEDIDO)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.maria()), 0, 10, 1, 1));

        /* act */
        PaginaResponse<UsuarioResponse> pagina = controller.listarUsuarios(1L, PEDIDO);

        /* assert */
        assertThat(pagina.conteudo()).extracting(UsuarioResponse::tipo).containsExactly("CLIENTE");
    }
}
