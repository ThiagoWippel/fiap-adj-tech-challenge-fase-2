package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeUsuarioDTO;
import br.com.fiap.restaurante.application.dto.NovoUsuarioDTO;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.dto.TrocaDeSenhaDTO;
import br.com.fiap.restaurante.application.dto.TrocaDeTipoDTO;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.restaurante.ListarRestaurantesDoUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.AtualizarUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.BuscarUsuarioPorIdUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.BuscarUsuariosPorNomePaginadoUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.BuscarUsuariosPorNomeUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.CadastrarUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.ExcluirUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.TrocarSenhaUseCase;
import br.com.fiap.restaurante.application.usecase.usuario.TrocarTipoDoUsuarioUseCase;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.gateway.RestauranteGateway;
import br.com.fiap.restaurante.interfaceadapter.gateway.TipoUsuarioGateway;
import br.com.fiap.restaurante.interfaceadapter.gateway.UsuarioGateway;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestaurantePresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioPresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;

import java.util.List;

/**
 * Monta gateways e casos de uso de usuário e converte o resultado com o
 * presenter. Não conhece HTTP: quem chama é o controller REST da infraestrutura.
 */
public class UsuarioController {

    private final IUsuarioDataSource usuarioDataSource;
    private final ITipoUsuarioDataSource tipoUsuarioDataSource;
    private final IRestauranteDataSource restauranteDataSource;
    private final IPasswordEncoder senhas;
    private final ITransactionManager transacao;

    private UsuarioController(IUsuarioDataSource usuarioDataSource, ITipoUsuarioDataSource tipoUsuarioDataSource,
                              IRestauranteDataSource restauranteDataSource, IPasswordEncoder senhas,
                              ITransactionManager transacao) {
        this.usuarioDataSource = usuarioDataSource;
        this.tipoUsuarioDataSource = tipoUsuarioDataSource;
        this.restauranteDataSource = restauranteDataSource;
        this.senhas = senhas;
        this.transacao = transacao;
    }

    public static UsuarioController create(IUsuarioDataSource usuarioDataSource,
                                           ITipoUsuarioDataSource tipoUsuarioDataSource,
                                           IRestauranteDataSource restauranteDataSource,
                                           IPasswordEncoder senhas, ITransactionManager transacao) {
        return new UsuarioController(usuarioDataSource, tipoUsuarioDataSource, restauranteDataSource, senhas,
                transacao);
    }

    public UsuarioResponse cadastrar(NovoUsuarioDTO dados) {
        var useCase = CadastrarUsuarioUseCase.create(usuarioGateway(),
                TipoUsuarioGateway.create(tipoUsuarioDataSource), senhas, transacao);
        return UsuarioPresenter.paraResposta(useCase.run(dados));
    }

    public UsuarioResponse buscarPorId(Long id) {
        var useCase = BuscarUsuarioPorIdUseCase.create(usuarioGateway());
        return UsuarioPresenter.paraResposta(useCase.run(id));
    }

    public List<UsuarioResponse> buscarPorNome(String nome) {
        var useCase = BuscarUsuariosPorNomeUseCase.create(usuarioGateway());
        return UsuarioPresenter.paraLista(useCase.run(nome));
    }

    public PaginaResponse<UsuarioResponse> buscarPorNomePaginado(String nome, PedidoDePagina pedido) {
        var useCase = BuscarUsuariosPorNomePaginadoUseCase.create(usuarioGateway());
        return UsuarioPresenter.paraPagina(useCase.run(nome, pedido));
    }

    public UsuarioResponse atualizar(AtualizacaoDeUsuarioDTO dados) {
        var useCase = AtualizarUsuarioUseCase.create(usuarioGateway(), transacao);
        return UsuarioPresenter.paraResposta(useCase.run(dados));
    }

    public void trocarSenha(TrocaDeSenhaDTO dados) {
        TrocarSenhaUseCase.create(usuarioGateway(), senhas, transacao).run(dados);
    }

    public UsuarioResponse trocarTipo(TrocaDeTipoDTO dados) {
        var useCase = TrocarTipoDoUsuarioUseCase.create(usuarioGateway(),
                TipoUsuarioGateway.create(tipoUsuarioDataSource), restauranteGateway(), transacao);
        return UsuarioPresenter.paraResposta(useCase.run(dados));
    }

    public void excluir(Long id) {
        ExcluirUsuarioUseCase.create(usuarioGateway(), restauranteGateway(), transacao).run(id);
    }

    public PaginaResponse<RestauranteResponse> listarRestaurantes(Long id, PedidoDePagina pedido) {
        var useCase = ListarRestaurantesDoUsuarioUseCase.create(usuarioGateway(), restauranteGateway());
        return RestaurantePresenter.paraPagina(useCase.run(id, pedido));
    }

    private UsuarioGateway usuarioGateway() {
        return UsuarioGateway.create(usuarioDataSource);
    }

    private RestauranteGateway restauranteGateway() {
        return RestauranteGateway.create(restauranteDataSource);
    }
}
