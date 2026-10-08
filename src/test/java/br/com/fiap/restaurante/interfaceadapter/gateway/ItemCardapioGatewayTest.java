package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosItemCardapio;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.feijoada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Gateway de itens do cardápio")
class ItemCardapioGatewayTest {

    @Mock
    private IItemCardapioDataSource dataSource;

    private AutoCloseable mocks;
    private ItemCardapioGateway gateway;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        gateway = ItemCardapioGateway.create(dataSource);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("ITE-01 · incluir e atualizar convertem o item nos dois sentidos")
    void deveConverterNosDoisSentidos() {
        /* arrange */
        when(dataSource.incluir(any())).thenReturn(DadosDeExemplo.feijoada());
        when(dataSource.atualizar(any())).thenReturn(DadosDeExemplo.feijoada());

        /* act */
        ItemCardapio incluido = gateway.incluir(feijoada());
        gateway.atualizar(feijoada());

        /* assert */
        ArgumentCaptor<DadosItemCardapio> enviados = ArgumentCaptor.forClass(DadosItemCardapio.class);
        verify(dataSource).incluir(enviados.capture());
        assertThat(enviados.getValue()).isEqualTo(DadosDeExemplo.feijoada());
        assertThat(incluido).usingRecursiveComparison().isEqualTo(feijoada());
    }

    @Test
    @DisplayName("ITE-21 · buscar e listar convertem os itens; as verificações e a remoção são repassadas")
    void deveConsultarERepassar() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());
        when(dataSource.buscarPorId(5L)).thenReturn(Optional.of(DadosDeExemplo.feijoada()));
        when(dataSource.listar(9L, null, pedido)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.feijoada()), 0, 10, 1, 1));
        when(dataSource.existeNomeAtivo(9L, "Feijoada")).thenReturn(true);
        when(dataSource.existeNomeAtivoEmOutroItem(9L, "Feijoada", 6L)).thenReturn(true);

        /* act */
        gateway.remover(5L);

        /* assert */
        assertThat(gateway.buscarPorId(5L)).get().extracting(ItemCardapio::getNome).isEqualTo("Feijoada");
        assertThat(gateway.buscarPorId(99L)).isEmpty();
        assertThat(gateway.listar(9L, null, pedido).conteudo()).extracting(ItemCardapio::getId).containsExactly(5L);
        assertThat(gateway.existeNomeAtivo(9L, "Feijoada")).isTrue();
        assertThat(gateway.existeNomeAtivoEmOutroItem(9L, "Feijoada", 6L)).isTrue();
        verify(dataSource).remover(5L);
    }
}
