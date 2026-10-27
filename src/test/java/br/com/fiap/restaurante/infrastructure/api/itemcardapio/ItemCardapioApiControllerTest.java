package br.com.fiap.restaurante.infrastructure.api.itemcardapio;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Endpoints de item do cardápio")
class ItemCardapioApiControllerTest {

    @Mock
    private IItemCardapioDataSource itens;

    @Mock
    private IRestauranteDataSource restaurantes;

    private AutoCloseable mocks;
    private ItemCardapioApiController api;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        api = new ItemCardapioApiController(itens, restaurantes, new TransacaoImediata());
        when(restaurantes.existeAtivo(9L)).thenReturn(true);
        when(itens.buscarPorId(5L)).thenReturn(Optional.of(DadosDeExemplo.feijoada()));
        when(itens.buscarPorIdParaAlterar(5L)).thenReturn(Optional.of(DadosDeExemplo.feijoada()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("ITE-21 · cadastrar devolve 201 com o Location aninhado no restaurante")
    void deveCadastrarComLocation() {
        /* arrange */
        when(itens.incluir(any())).thenReturn(DadosDeExemplo.feijoada());
        ItemCardapioRequest requisicao = new ItemCardapioRequest("Feijoada", "Feijoada completa com farofa e couve.",
                new BigDecimal("39.90"), true, "fotos/feijoada.jpg");

        /* act */
        ResponseEntity<ItemCardapioResponse> resposta =
                api.cadastrar(9L, requisicao, UriComponentsBuilder.fromUriString("http://localhost:8080"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation())
                .hasToString("http://localhost:8080/api/v1/restaurantes/9/itens-cardapio/5");
        assertThat(resposta.getBody().restauranteId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("ITE-21 · listar com o filtro apenasNoLocal e buscar por id devolvem 200")
    void deveConsultar() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("preco", false)));
        when(itens.listar(9L, true, pedido)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.feijoada()), 0, 10, 1, 1));

        /* act */
        ResponseEntity<PaginaResponse<ItemCardapioResponse>> pagina =
                api.listar(9L, true, PageRequest.of(0, 10, Sort.by(Sort.Order.desc("preco"))));
        ResponseEntity<ItemCardapioResponse> porId = api.buscarPorId(9L, 5L);

        /* assert */
        assertThat(pagina.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pagina.getBody().conteudo()).extracting(ItemCardapioResponse::nome).containsExactly("Feijoada");
        assertThat(porId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(porId.getBody().preco()).isEqualByComparingTo("39.90");
    }

    @Test
    @DisplayName("ITE-21 · atualizar devolve 200 com o item gravado; excluir devolve 204")
    void deveAtualizarEExcluir() {
        /* arrange */
        when(itens.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        ItemCardapioRequest requisicao = new ItemCardapioRequest("Feijoada da casa", "Feijoada para duas pessoas.",
                new BigDecimal("42.50"), false, "fotos/f.png");

        /* act */
        ResponseEntity<ItemCardapioResponse> atualizado = api.atualizar(9L, 5L, requisicao);
        ResponseEntity<Void> excluido = api.excluir(9L, 5L);

        /* assert */
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(atualizado.getBody().nome()).isEqualTo("Feijoada da casa");
        assertThat(atualizado.getBody().apenasNoLocal()).isFalse();
        assertThat(excluido.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(itens).remover(5L);
    }
}
