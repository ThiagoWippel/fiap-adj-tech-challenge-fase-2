package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.FiltroDeRestaurantes;
import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;

/**
 * Lista os restaurantes ativos, paginados, com filtro opcional por trecho do nome
 * e por tipo de cozinha.
 */
public class ListarRestaurantesUseCase {

    private final IRestauranteGateway restaurantes;

    private ListarRestaurantesUseCase(IRestauranteGateway restaurantes) {
        this.restaurantes = restaurantes;
    }

    public static ListarRestaurantesUseCase create(IRestauranteGateway restaurantes) {
        return new ListarRestaurantesUseCase(restaurantes);
    }

    public Pagina<Restaurante> run(FiltroDeRestaurantes filtro, PedidoDePagina pedido) {
        String nome = filtro.nome() == null ? "" : filtro.nome().trim();
        TipoCozinha tipo = filtro.tipoCozinha() == null || filtro.tipoCozinha().isBlank()
                ? null : TipoCozinha.de(filtro.tipoCozinha());
        return restaurantes.listar(nome, tipo, pedido);
    }
}
