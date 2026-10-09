package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;

/**
 * Exclui um item logicamente: a linha fica, marcada como removida, e o nome fica
 * livre para outro item do restaurante.
 */
public class ExcluirItemCardapioUseCase {

    private final IItemCardapioGateway itens;
    private final IRestauranteGateway restaurantes;
    private final ITransactionManager transacao;

    private ExcluirItemCardapioUseCase(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                       ITransactionManager transacao) {
        this.itens = itens;
        this.restaurantes = restaurantes;
        this.transacao = transacao;
    }

    public static ExcluirItemCardapioUseCase create(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                                    ITransactionManager transacao) {
        return new ExcluirItemCardapioUseCase(itens, restaurantes, transacao);
    }

    public void run(Long restauranteId, Long itemId) {
        transacao.executar(() -> {
            BuscarItemCardapioUseCase.itemDoRestauranteParaAlterar(itens, restaurantes, restauranteId, itemId);
            itens.remover(itemId);
        });
    }
}
