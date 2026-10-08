package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;

/**
 * Exclui um restaurante logicamente: a linha fica, marcada como removida, porque
 * pedidos e avaliações das próximas fases vão apontar para ela.
 */
public class ExcluirRestauranteUseCase {

    private final IRestauranteGateway restaurantes;
    private final ITransactionManager transacao;

    private ExcluirRestauranteUseCase(IRestauranteGateway restaurantes, ITransactionManager transacao) {
        this.restaurantes = restaurantes;
        this.transacao = transacao;
    }

    public static ExcluirRestauranteUseCase create(IRestauranteGateway restaurantes, ITransactionManager transacao) {
        return new ExcluirRestauranteUseCase(restaurantes, transacao);
    }

    public void run(Long id) {
        transacao.executar(() -> {
            restaurantes.buscarPorId(id).orElseThrow(() -> BuscarRestaurantePorIdUseCase.restauranteNaoEncontrado(id));
            restaurantes.remover(id);
        });
    }
}
