package br.com.fiap.restaurante.application.port;

import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Emite o token de acesso devolvido no login.
 */
public interface ITokenGenerator {

    TokenDeAcesso gerar(Usuario usuario);
}
