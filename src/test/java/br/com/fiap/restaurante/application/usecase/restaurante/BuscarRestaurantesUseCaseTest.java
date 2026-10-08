package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.FiltroDeRestaurantes;
import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.ana;
import static br.com.fiap.restaurante.suporte.Exemplos.cantina;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Consultas de restaurante")
class BuscarRestaurantesUseCaseTest {

    private static final PedidoDePagina PEDIDO = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));
    private static final Pagina<Restaurante> PAGINA = new Pagina<>(List.of(cantina()), 0, 10, 1, 1);

    @Mock
    private IRestauranteGateway restaurantes;

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        when(restaurantes.buscarPorId(9L)).thenReturn(Optional.of(cantina()));
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(ana()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-14 · buscar por id devolve o restaurante; inexistente ou removido devolve não encontrado")
    void deveBuscarPorId() {
        /* act + assert */
        assertThat(BuscarRestaurantePorIdUseCase.create(restaurantes).run(9L).getNome()).isEqualTo("Cantina da Nona");
        assertThatThrownBy(() -> BuscarRestaurantePorIdUseCase.create(restaurantes).run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Restaurante 99 não encontrado.");
    }

    @Test
    @DisplayName("RES-13 · a listagem apara o nome e converte o tipo de cozinha do filtro")
    void deveListarComFiltros() {
        /* arrange */
        when(restaurantes.listar("nona", TipoCozinha.ITALIANA, PEDIDO)).thenReturn(PAGINA);

        /* act */
        Pagina<Restaurante> resultado = ListarRestaurantesUseCase.create(restaurantes)
                .run(new FiltroDeRestaurantes("  nona ", "italiana"), PEDIDO);

        /* assert */
        assertThat(resultado).isSameAs(PAGINA);
    }

    @Test
    @DisplayName("RES-13 · sem filtros, lista todos os restaurantes ativos")
    void deveListarSemFiltros() {
        /* act */
        ListarRestaurantesUseCase.create(restaurantes).run(new FiltroDeRestaurantes(null, " "), PEDIDO);

        /* assert */
        verify(restaurantes).listar("", null, PEDIDO);
    }

    @Test
    @DisplayName("COZ-03 · tipo de cozinha inválido no filtro é recusado")
    void deveRecusarFiltroDeCozinhaInvalido() {
        /* act + assert */
        assertThatThrownBy(() -> ListarRestaurantesUseCase.create(restaurantes)
                .run(new FiltroDeRestaurantes(null, "TAILANDESA"), PEDIDO))
                .isInstanceOf(ValidacaoDeDominioException.class);
        verify(restaurantes, never()).listar(any(), any(), any());
    }

    @Test
    @DisplayName("RES-17 · lista os restaurantes ativos do usuário; usuário inexistente devolve não encontrado")
    void deveListarRestaurantesDoUsuario() {
        /* arrange */
        when(restaurantes.buscarPorDono(8L, PEDIDO)).thenReturn(PAGINA);
        ListarRestaurantesDoUsuarioUseCase useCase = ListarRestaurantesDoUsuarioUseCase.create(usuarios, restaurantes);

        /* act + assert */
        assertThat(useCase.run(8L, PEDIDO)).isSameAs(PAGINA);
        assertThatThrownBy(() -> useCase.run(99L, PEDIDO))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        verify(restaurantes, times(1)).buscarPorDono(any(), any());
    }
}
