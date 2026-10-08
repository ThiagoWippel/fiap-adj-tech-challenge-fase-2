package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.dto.RenomeacaoDeTipoUsuarioDTO;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.tipousuario.BuscarTipoUsuarioPorIdUseCase;
import br.com.fiap.restaurante.application.usecase.tipousuario.CadastrarTipoUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.tipousuario.ExcluirTipoUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.tipousuario.ListarTiposUsuarioUseCase;
import br.com.fiap.restaurante.application.usecase.tipousuario.ListarUsuariosDoTipoUseCase;
import br.com.fiap.restaurante.application.usecase.tipousuario.RenomearTipoUsuarioUseCase;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.gateway.TipoUsuarioGateway;
import br.com.fiap.restaurante.interfaceadapter.gateway.UsuarioGateway;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.TipoUsuarioPresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.TipoUsuarioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioPresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;

/**
 * Monta gateways e casos de uso de tipo de usuário e converte o resultado com os
 * presenters.
 */
public class TipoUsuarioController {

    private final ITipoUsuarioDataSource tipoUsuarioDataSource;
    private final IUsuarioDataSource usuarioDataSource;
    private final ITransactionManager transacao;

    private TipoUsuarioController(ITipoUsuarioDataSource tipoUsuarioDataSource, IUsuarioDataSource usuarioDataSource,
                                  ITransactionManager transacao) {
        this.tipoUsuarioDataSource = tipoUsuarioDataSource;
        this.usuarioDataSource = usuarioDataSource;
        this.transacao = transacao;
    }

    public static TipoUsuarioController create(ITipoUsuarioDataSource tipoUsuarioDataSource,
                                               IUsuarioDataSource usuarioDataSource, ITransactionManager transacao) {
        return new TipoUsuarioController(tipoUsuarioDataSource, usuarioDataSource, transacao);
    }

    public TipoUsuarioResponse cadastrar(String nome) {
        var useCase = CadastrarTipoUsuarioUseCase.create(tipoGateway(), transacao);
        return TipoUsuarioPresenter.paraResposta(useCase.run(nome));
    }

    public TipoUsuarioResponse buscarPorId(Long id) {
        var useCase = BuscarTipoUsuarioPorIdUseCase.create(tipoGateway());
        return TipoUsuarioPresenter.paraResposta(useCase.run(id));
    }

    public PaginaResponse<TipoUsuarioResponse> listar(PedidoDePagina pedido) {
        var useCase = ListarTiposUsuarioUseCase.create(tipoGateway());
        return TipoUsuarioPresenter.paraPagina(useCase.run(pedido));
    }

    public TipoUsuarioResponse renomear(RenomeacaoDeTipoUsuarioDTO dados) {
        var useCase = RenomearTipoUsuarioUseCase.create(tipoGateway(), transacao);
        return TipoUsuarioPresenter.paraResposta(useCase.run(dados));
    }

    public void excluir(Long id) {
        ExcluirTipoUsuarioUseCase.create(tipoGateway(), UsuarioGateway.create(usuarioDataSource), transacao).run(id);
    }

    public PaginaResponse<UsuarioResponse> listarUsuarios(Long tipoId, PedidoDePagina pedido) {
        var useCase = ListarUsuariosDoTipoUseCase.create(tipoGateway(), UsuarioGateway.create(usuarioDataSource));
        return UsuarioPresenter.paraPagina(useCase.run(tipoId, pedido));
    }

    private TipoUsuarioGateway tipoGateway() {
        return TipoUsuarioGateway.create(tipoUsuarioDataSource);
    }
}
