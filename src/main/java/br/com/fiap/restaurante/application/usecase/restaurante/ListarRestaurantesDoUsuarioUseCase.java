package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.usecase.usuario.BuscarUsuarioPorIdUseCase;
import br.com.fiap.restaurante.domain.entity.Restaurante;

/**
 * Lista, paginados, os restaurantes ativos de um usuário.
 */
public class ListarRestaurantesDoUsuarioUseCase {

    private final IUsuarioGateway usuarios;
    private final IRestauranteGateway restaurantes;

    private ListarRestaurantesDoUsuarioUseCase(IUsuarioGateway usuarios, IRestauranteGateway restaurantes) {
        this.usuarios = usuarios;
        this.restaurantes = restaurantes;
    }

    public static ListarRestaurantesDoUsuarioUseCase create(IUsuarioGateway usuarios,
                                                            IRestauranteGateway restaurantes) {
        return new ListarRestaurantesDoUsuarioUseCase(usuarios, restaurantes);
    }

    public Pagina<Restaurante> run(Long usuarioId, PedidoDePagina pedido) {
        usuarios.buscarPorId(usuarioId).orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(usuarioId));
        return restaurantes.buscarPorDono(usuarioId, pedido);
    }
}
