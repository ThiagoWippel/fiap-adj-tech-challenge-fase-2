package br.com.fiap.restaurante.application.usecase.autenticacao;

import br.com.fiap.restaurante.application.dto.Autenticacao;
import br.com.fiap.restaurante.application.dto.CredenciaisDTO;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.util.Optional;

/**
 * Confere login e senha e emite o token de acesso.
 *
 * <p>Login inexistente e senha errada dão a mesma resposta, e a senha é
 * conferida nos dois casos, para que nem a mensagem nem o tempo de resposta
 * revelem quais logins existem.
 */
public class AutenticarUsuarioUseCase {

    private static final String CREDENCIAIS_INVALIDAS = "Login ou senha inválidos.";

    private final IUsuarioGateway usuarios;
    private final IPasswordEncoder senhas;
    private final ITokenGenerator tokens;

    private AutenticarUsuarioUseCase(IUsuarioGateway usuarios, IPasswordEncoder senhas, ITokenGenerator tokens) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
    }

    public static AutenticarUsuarioUseCase create(IUsuarioGateway usuarios, IPasswordEncoder senhas,
                                                  ITokenGenerator tokens) {
        return new AutenticarUsuarioUseCase(usuarios, senhas, tokens);
    }

    public Autenticacao run(CredenciaisDTO credenciais) {
        if (vazio(credenciais.login()) || vazio(credenciais.senha())) {
            throw new ValidacaoDeDominioException("Login e senha são obrigatórios.");
        }

        Optional<Usuario> usuario = usuarios.buscarPorLogin(credenciais.login());
        boolean senhaConfere = senhas.confere(credenciais.senha(), usuario.map(Usuario::getSenha).orElse(null));
        if (usuario.isEmpty() || !senhaConfere) {
            throw new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS);
        }

        return new Autenticacao(usuario.get(), tokens.gerar(usuario.get()));
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
