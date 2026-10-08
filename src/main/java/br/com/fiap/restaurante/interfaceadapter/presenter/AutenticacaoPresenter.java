package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Autenticacao;
import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Converte o resultado do login na resposta da API.
 */
public final class AutenticacaoPresenter {

    private AutenticacaoPresenter() {
    }

    public static LoginResponse paraResposta(Autenticacao autenticacao) {
        Usuario usuario = autenticacao.usuario();
        return new LoginResponse(usuario.getId(), usuario.getNome(), usuario.getTipo().getCodigo(),
                autenticacao.token().valor(), UsuarioPresenter.emSegundos(autenticacao.token().expiraEm()));
    }
}
