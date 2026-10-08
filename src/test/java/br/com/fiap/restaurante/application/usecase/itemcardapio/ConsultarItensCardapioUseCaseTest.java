package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.feijoada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Consultas de item do cardápio")
class ConsultarItensCardapioUseCaseTest {

    private static final PedidoDePagina PEDIDO = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));

    @Mock
    private IItemCardapioGateway itens;

    @Mock
    private IRestauranteGateway restaurantes;

    private AutoCloseable mocks;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        when(restaurantes.existeAtivo(9L)).thenReturn(true);
        when(restaurantes.existeAtivo(10L)).thenReturn(true);
        when(itens.buscarPorId(5L)).thenReturn(Optional.of(feijoada()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("ITE-21 · buscar o item pela rota do restaurante dele devolve o item")
    void deveBuscarPorId() {
        /* act */
        ItemCardapio item = BuscarItemCardapioUseCase.create(itens, restaurantes).run(9L, 5L);

        /* assert */
        assertThat(item.getNome()).isEqualTo("Feijoada");
    }

    @Test
    @DisplayName("ITE-17 · pela rota de outro restaurante, o item não é encontrado")
    void deveRecusarItemDeOutroRestaurante() {
        /* act + assert */
        assertThatThrownBy(() -> BuscarItemCardapioUseCase.create(itens, restaurantes).run(10L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Item 5 não encontrado no restaurante 10.");
        assertThatThrownBy(() -> BuscarItemCardapioUseCase.create(itens, restaurantes).run(9L, 99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Item 99 não encontrado no restaurante 9.");
    }

    @Test
    @DisplayName("ITE-22 · com o restaurante removido, as consultas de item devolvem não encontrado")
    void deveRecusarRestauranteRemovido() {
        /* act + assert */
        assertThatThrownBy(() -> BuscarItemCardapioUseCase.create(itens, restaurantes).run(99L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Restaurante 99 não encontrado.");
        assertThatThrownBy(() -> ListarItensCardapioUseCase.create(itens, restaurantes).run(99L, null, PEDIDO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(itens, never()).buscarPorId(anyLong());
        verify(itens, never()).listar(any(), any(), any());
    }

    @Test
    @DisplayName("ITE-21 · a listagem repassa o filtro de disponibilidade e o pedido de página")
    void deveListar() {
        /* arrange */
        Pagina<ItemCardapio> pagina = new Pagina<>(List.of(feijoada()), 0, 10, 1, 1);
        when(itens.listar(9L, true, PEDIDO)).thenReturn(pagina);

        /* act */
        Pagina<ItemCardapio> resultado = ListarItensCardapioUseCase.create(itens, restaurantes).run(9L, true, PEDIDO);

        /* assert */
        assertThat(resultado).isSameAs(pagina);
    }
}
