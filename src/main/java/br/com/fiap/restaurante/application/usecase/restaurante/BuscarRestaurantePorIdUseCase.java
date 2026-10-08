package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.Restaurante;

/**
 * Consulta um restaurante pelo id. Restaurante removido é tratado como
 * inexistente.
 */
public class BuscarRestaurantePorIdUseCase {

    private final IRestauranteGateway restaurantes;

    private BuscarRestaurantePorIdUseCase(IRestauranteGateway restaurantes) {
        this.restaurantes = restaurantes;
    }

    public static BuscarRestaurantePorIdUseCase create(IRestauranteGateway restaurantes) {
        return new BuscarRestaurantePorIdUseCase(restaurantes);
    }

    public Restaurante run(Long id) {
        return restaurantes.buscarPorId(id).orElseThrow(() -> restauranteNaoEncontrado(id));
    }

    public static RecursoNaoEncontradoException restauranteNaoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Restaurante " + id + " não encontrado.");
    }
}
