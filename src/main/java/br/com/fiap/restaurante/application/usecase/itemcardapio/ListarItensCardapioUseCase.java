package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;

/**
 * Lista, paginados, os itens ativos do cardápio de um restaurante ativo, com
 * filtro opcional pela disponibilidade só no local.
 */
public class ListarItensCardapioUseCase {

    private final IItemCardapioGateway itens;
    private final IRestauranteGateway restaurantes;

    private ListarItensCardapioUseCase(IItemCardapioGateway itens, IRestauranteGateway restaurantes) {
        this.itens = itens;
        this.restaurantes = restaurantes;
    }

    public static ListarItensCardapioUseCase create(IItemCardapioGateway itens, IRestauranteGateway restaurantes) {
        return new ListarItensCardapioUseCase(itens, restaurantes);
    }

    public Pagina<ItemCardapio> run(Long restauranteId, Boolean apenasNoLocal, PedidoDePagina pedido) {
        BuscarItemCardapioUseCase.garantirRestauranteAtivo(restaurantes, restauranteId);
        return itens.listar(restauranteId, apenasNoLocal, pedido);
    }
}
