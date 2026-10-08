package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeItemCardapioDTO;
import br.com.fiap.restaurante.application.dto.NovoItemCardapioDTO;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.itemcardapio.AtualizarItemCardapioUseCase;
import br.com.fiap.restaurante.application.usecase.itemcardapio.BuscarItemCardapioUseCase;
import br.com.fiap.restaurante.application.usecase.itemcardapio.CadastrarItemCardapioUseCase;
import br.com.fiap.restaurante.application.usecase.itemcardapio.ExcluirItemCardapioUseCase;
import br.com.fiap.restaurante.application.usecase.itemcardapio.ListarItensCardapioUseCase;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.gateway.ItemCardapioGateway;
import br.com.fiap.restaurante.interfaceadapter.gateway.RestauranteGateway;
import br.com.fiap.restaurante.interfaceadapter.presenter.ItemCardapioPresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.ItemCardapioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;

/**
 * Monta gateways e casos de uso de item do cardápio e converte o resultado com o
 * presenter.
 */
public class ItemCardapioController {

    private final IItemCardapioDataSource itemDataSource;
    private final IRestauranteDataSource restauranteDataSource;
    private final ITransactionManager transacao;

    private ItemCardapioController(IItemCardapioDataSource itemDataSource, IRestauranteDataSource restauranteDataSource,
                                   ITransactionManager transacao) {
        this.itemDataSource = itemDataSource;
        this.restauranteDataSource = restauranteDataSource;
        this.transacao = transacao;
    }

    public static ItemCardapioController create(IItemCardapioDataSource itemDataSource,
                                                IRestauranteDataSource restauranteDataSource,
                                                ITransactionManager transacao) {
        return new ItemCardapioController(itemDataSource, restauranteDataSource, transacao);
    }

    public ItemCardapioResponse cadastrar(NovoItemCardapioDTO dados) {
        var useCase = CadastrarItemCardapioUseCase.create(itemGateway(), restauranteGateway(), transacao);
        return ItemCardapioPresenter.paraResposta(useCase.run(dados));
    }

    public ItemCardapioResponse buscarPorId(Long restauranteId, Long itemId) {
        var useCase = BuscarItemCardapioUseCase.create(itemGateway(), restauranteGateway());
        return ItemCardapioPresenter.paraResposta(useCase.run(restauranteId, itemId));
    }

    public PaginaResponse<ItemCardapioResponse> listar(Long restauranteId, Boolean apenasNoLocal,
                                                       PedidoDePagina pedido) {
        var useCase = ListarItensCardapioUseCase.create(itemGateway(), restauranteGateway());
        return ItemCardapioPresenter.paraPagina(useCase.run(restauranteId, apenasNoLocal, pedido));
    }

    public ItemCardapioResponse atualizar(AtualizacaoDeItemCardapioDTO dados) {
        var useCase = AtualizarItemCardapioUseCase.create(itemGateway(), restauranteGateway(), transacao);
        return ItemCardapioPresenter.paraResposta(useCase.run(dados));
    }

    public void excluir(Long restauranteId, Long itemId) {
        ExcluirItemCardapioUseCase.create(itemGateway(), restauranteGateway(), transacao).run(restauranteId, itemId);
    }

    private ItemCardapioGateway itemGateway() {
        return ItemCardapioGateway.create(itemDataSource);
    }

    private RestauranteGateway restauranteGateway() {
        return RestauranteGateway.create(restauranteDataSource);
    }
}
