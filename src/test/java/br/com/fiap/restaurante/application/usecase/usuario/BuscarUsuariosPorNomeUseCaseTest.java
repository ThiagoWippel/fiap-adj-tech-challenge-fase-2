package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Buscar usuários por nome")
class BuscarUsuariosPorNomeUseCaseTest {

    @Mock
    private IUsuarioGateway usuarios;

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
    @DisplayName("USU-17 · tira os espaços das pontas do termo antes de buscar")
    void deveAparOTermo() {
        /* arrange */
        when(usuarios.buscarPorNome("maria")).thenReturn(List.of(maria()));

        /* act */
        List<Usuario> encontrados = BuscarUsuariosPorNomeUseCase.create(usuarios).run("  maria ");

        /* assert */
        assertThat(encontrados).extracting(Usuario::getNome).containsExactly("Maria Silva");
    }

    @Test
    @DisplayName("USU-17 · termo ausente busca todos os usuários ativos")
    void deveBuscarTodos_QuandoNaoHouverTermo() {
        /* act */
        BuscarUsuariosPorNomeUseCase.create(usuarios).run(null);

        /* assert */
        verify(usuarios).buscarPorNome("");
    }

    @Test
    @DisplayName("USU-28 · a busca paginada apara o termo e repassa o pedido de página")
    void deveBuscarPaginado() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));
        Pagina<Usuario> pagina = new Pagina<>(List.of(maria()), 0, 10, 1, 1);
        when(usuarios.buscarPorNome("maria", pedido)).thenReturn(pagina);

        /* act */
        Pagina<Usuario> resultado = BuscarUsuariosPorNomePaginadoUseCase.create(usuarios).run(" maria ", pedido);

        /* assert */
        assertThat(resultado).isSameAs(pagina);
    }

    @Test
    @DisplayName("USU-28 · a busca paginada sem termo busca todos os usuários ativos")
    void deveBuscarTodosPaginado_QuandoNaoHouverTermo() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());

        /* act */
        BuscarUsuariosPorNomePaginadoUseCase.create(usuarios).run(null, pedido);

        /* assert */
        verify(usuarios).buscarPorNome("", pedido);
    }
}
