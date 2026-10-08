package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Consultas de tipo de usuário")
class BuscarTipoUsuarioUseCaseTest {

    private static final PedidoDePagina PEDIDO = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));

    @Mock
    private ITipoUsuarioGateway tipos;

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        when(tipos.buscarPorId(1L)).thenReturn(Optional.of(cliente()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-16 · buscar por id devolve o tipo; id inexistente devolve não encontrado")
    void deveBuscarPorId() {
        /* act */
        TipoUsuario tipo = BuscarTipoUsuarioPorIdUseCase.create(tipos).run(1L);

        /* assert */
        assertThat(tipo.getCodigo()).isEqualTo("CLIENTE");
        assertThatThrownBy(() -> BuscarTipoUsuarioPorIdUseCase.create(tipos).run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Tipo de usuário 99 não encontrado.");
    }

    @Test
    @DisplayName("TIP-16 · a listagem repassa o pedido de página")
    void deveListar() {
        /* arrange */
        Pagina<TipoUsuario> pagina = new Pagina<>(List.of(cliente()), 0, 10, 1, 1);
        when(tipos.listar(PEDIDO)).thenReturn(pagina);

        /* act */
        Pagina<TipoUsuario> resultado = ListarTiposUsuarioUseCase.create(tipos).run(PEDIDO);

        /* assert */
        assertThat(resultado).isSameAs(pagina);
    }

    @Test
    @DisplayName("TIP-17 · lista os usuários ativos do tipo; tipo inexistente devolve não encontrado")
    void deveListarOsUsuariosDoTipo() {
        /* arrange */
        Pagina<Usuario> pagina = new Pagina<>(List.of(maria()), 0, 10, 1, 1);
        when(usuarios.buscarPorTipo(1L, PEDIDO)).thenReturn(pagina);
        ListarUsuariosDoTipoUseCase useCase = ListarUsuariosDoTipoUseCase.create(tipos, usuarios);

        /* act */
        Pagina<Usuario> resultado = useCase.run(1L, PEDIDO);

        /* assert */
        assertThat(resultado).isSameAs(pagina);
        assertThatThrownBy(() -> useCase.run(99L, PEDIDO))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Tipo de usuário 99 não encontrado.");
        verify(usuarios, times(1)).buscarPorTipo(any(), any());
    }
}
