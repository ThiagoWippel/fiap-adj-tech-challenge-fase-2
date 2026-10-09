package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.usecase.restaurante.BuscarRestaurantePorIdUseCase;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;

import java.util.Optional;

/**
 * Consulta um item pela rota do restaurante. O restaurante precisa estar ativo e
 * ser o dono do item; pela rota de outro restaurante, o item não existe.
 */
public class BuscarItemCardapioUseCase {

    private final IItemCardapioGateway itens;
    private final IRestauranteGateway restaurantes;

    private BuscarItemCardapioUseCase(IItemCardapioGateway itens, IRestauranteGateway restaurantes) {
        this.itens = itens;
        this.restaurantes = restaurantes;
    }

    public static BuscarItemCardapioUseCase create(IItemCardapioGateway itens, IRestauranteGateway restaurantes) {
        return new BuscarItemCardapioUseCase(itens, restaurantes);
    }

    public ItemCardapio run(Long restauranteId, Long itemId) {
        return itemDoRestaurante(itens, restaurantes, restauranteId, itemId);
    }

    static ItemCardapio itemDoRestaurante(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                          Long restauranteId, Long itemId) {
        garantirRestauranteAtivo(restaurantes, restauranteId);
        return doRestaurante(itens.buscarPorId(itemId), restauranteId, itemId);
    }

    /** Como {@link #itemDoRestaurante}, mas reserva o item até o fim da transação. */
    static ItemCardapio itemDoRestauranteParaAlterar(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                                     Long restauranteId, Long itemId) {
        garantirRestauranteAtivo(restaurantes, restauranteId);
        return doRestaurante(itens.buscarPorIdParaAlterar(itemId), restauranteId, itemId);
    }

    private static ItemCardapio doRestaurante(Optional<ItemCardapio> item, Long restauranteId, Long itemId) {
        return item.filter(encontrado -> encontrado.pertenceA(restauranteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item " + itemId + " não encontrado no restaurante " + restauranteId + "."));
    }

    static void garantirRestauranteAtivo(IRestauranteGateway restaurantes, Long restauranteId) {
        if (!restaurantes.existeAtivo(restauranteId)) {
            throw BuscarRestaurantePorIdUseCase.restauranteNaoEncontrado(restauranteId);
        }
    }

    static ConflitoDeDadosException nomeEmUso(Long restauranteId, String nome) {
        return new ConflitoDeDadosException(
                "O restaurante " + restauranteId + " já tem um item ativo chamado " + nome + ".");
    }
}
