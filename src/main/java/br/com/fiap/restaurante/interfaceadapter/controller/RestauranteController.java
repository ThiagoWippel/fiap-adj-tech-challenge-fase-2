package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeRestauranteDTO;
import br.com.fiap.restaurante.application.dto.FiltroDeRestaurantes;
import br.com.fiap.restaurante.application.dto.NovoRestauranteDTO;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.restaurante.AtualizarRestauranteUseCase;
import br.com.fiap.restaurante.application.usecase.restaurante.BuscarRestaurantePorIdUseCase;
import br.com.fiap.restaurante.application.usecase.restaurante.CadastrarRestauranteUseCase;
import br.com.fiap.restaurante.application.usecase.restaurante.ExcluirRestauranteUseCase;
import br.com.fiap.restaurante.application.usecase.restaurante.ListarRestaurantesUseCase;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.gateway.RestauranteGateway;
import br.com.fiap.restaurante.interfaceadapter.gateway.UsuarioGateway;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestaurantePresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;

/**
 * Monta gateways e casos de uso de restaurante e converte o resultado com o
 * presenter.
 */
public class RestauranteController {

    private final IRestauranteDataSource restauranteDataSource;
    private final IUsuarioDataSource usuarioDataSource;
    private final ITransactionManager transacao;

    private RestauranteController(IRestauranteDataSource restauranteDataSource, IUsuarioDataSource usuarioDataSource,
                                  ITransactionManager transacao) {
        this.restauranteDataSource = restauranteDataSource;
        this.usuarioDataSource = usuarioDataSource;
        this.transacao = transacao;
    }

    public static RestauranteController create(IRestauranteDataSource restauranteDataSource,
                                               IUsuarioDataSource usuarioDataSource, ITransactionManager transacao) {
        return new RestauranteController(restauranteDataSource, usuarioDataSource, transacao);
    }

    public RestauranteResponse cadastrar(NovoRestauranteDTO dados) {
        var useCase = CadastrarRestauranteUseCase.create(restauranteGateway(), usuarioGateway(), transacao);
        return RestaurantePresenter.paraResposta(useCase.run(dados));
    }

    public RestauranteResponse buscarPorId(Long id) {
        return RestaurantePresenter.paraResposta(BuscarRestaurantePorIdUseCase.create(restauranteGateway()).run(id));
    }

    public PaginaResponse<RestauranteResponse> listar(FiltroDeRestaurantes filtro, PedidoDePagina pedido) {
        var useCase = ListarRestaurantesUseCase.create(restauranteGateway());
        return RestaurantePresenter.paraPagina(useCase.run(filtro, pedido));
    }

    public RestauranteResponse atualizar(AtualizacaoDeRestauranteDTO dados) {
        var useCase = AtualizarRestauranteUseCase.create(restauranteGateway(), usuarioGateway(), transacao);
        return RestaurantePresenter.paraResposta(useCase.run(dados));
    }

    public void excluir(Long id) {
        ExcluirRestauranteUseCase.create(restauranteGateway(), transacao).run(id);
    }

    private RestauranteGateway restauranteGateway() {
        return RestauranteGateway.create(restauranteDataSource);
    }

    private UsuarioGateway usuarioGateway() {
        return UsuarioGateway.create(usuarioDataSource);
    }
}
