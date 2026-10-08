package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.CredenciaisDTO;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.application.usecase.autenticacao.AutenticarUsuarioUseCase;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.gateway.UsuarioGateway;
import br.com.fiap.restaurante.interfaceadapter.presenter.AutenticacaoPresenter;
import br.com.fiap.restaurante.interfaceadapter.presenter.LoginResponse;

public class AutenticacaoController {

    private final IUsuarioDataSource usuarioDataSource;
    private final IPasswordEncoder senhas;
    private final ITokenGenerator tokens;

    private AutenticacaoController(IUsuarioDataSource usuarioDataSource, IPasswordEncoder senhas,
                                   ITokenGenerator tokens) {
        this.usuarioDataSource = usuarioDataSource;
        this.senhas = senhas;
        this.tokens = tokens;
    }

    public static AutenticacaoController create(IUsuarioDataSource usuarioDataSource, IPasswordEncoder senhas,
                                                ITokenGenerator tokens) {
        return new AutenticacaoController(usuarioDataSource, senhas, tokens);
    }

    public LoginResponse autenticar(CredenciaisDTO credenciais) {
        var useCase = AutenticarUsuarioUseCase.create(UsuarioGateway.create(usuarioDataSource), senhas, tokens);
        return AutenticacaoPresenter.paraResposta(useCase.run(credenciais));
    }
}
