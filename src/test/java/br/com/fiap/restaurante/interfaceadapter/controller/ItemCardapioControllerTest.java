package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeItemCardapioDTO;
import br.com.fiap.restaurante.application.dto.NovoItemCardapioDTO;
import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.ItemCardapioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Controller de itens do cardápio")
class ItemCardapioControllerTest {

    @Mock
    private IItemCardapioDataSource itens;

    @Mock
    private IRestauranteDataSource restaurantes;

    private AutoCloseable mocks;
    private ItemCardapioController controller;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = ItemCardapioController.create(itens, restaurantes, new TransacaoImediata());
        when(restaurantes.existeAtivo(9L)).thenReturn(true);
        when(itens.buscarPorId(5L)).thenReturn(Optional.of(DadosDeExemplo.feijoada()));
        when(itens.buscarPorIdParaAlterar(5L)).thenReturn(Optional.of(DadosDeExemplo.feijoada()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("ITE-21 · cadastrar, consultar e listar devolvem as respostas")
    void deveCadastrarEConsultar() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());
        when(itens.incluir(any())).thenReturn(DadosDeExemplo.feijoada());
        when(itens.listar(9L, true, pedido)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.feijoada()), 0, 10, 1, 1));

        /* act */
        ItemCardapioResponse cadastrado = controller.cadastrar(new NovoItemCardapioDTO(9L, "Feijoada",
                "Feijoada completa com farofa e couve.", new BigDecimal("39.90"), true, "fotos/feijoada.jpg"));
        ItemCardapioResponse porId = controller.buscarPorId(9L, 5L);
        PaginaResponse<ItemCardapioResponse> pagina = controller.listar(9L, true, pedido);

        /* assert */
        assertThat(cadastrado.id()).isEqualTo(5L);
        assertThat(porId.nome()).isEqualTo("Feijoada");
        assertThat(pagina.conteudo()).hasSize(1);
    }

    @Test
    @DisplayName("ITE-21 · atualizar devolve o item gravado; excluir remove logicamente")
    void deveAtualizarEExcluir() {
        /* arrange */
        when(itens.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        ItemCardapioResponse atualizado = controller.atualizar(new AtualizacaoDeItemCardapioDTO(9L, 5L,
                "Feijoada da casa", "Feijoada para duas pessoas.", new BigDecimal("42.50"), false, "fotos/f.png"));
        controller.excluir(9L, 5L);

        /* assert */
        assertThat(atualizado.nome()).isEqualTo("Feijoada da casa");
        verify(itens).remover(5L);
    }
}
