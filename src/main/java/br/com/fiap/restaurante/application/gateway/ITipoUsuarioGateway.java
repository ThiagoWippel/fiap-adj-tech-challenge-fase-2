package br.com.fiap.restaurante.application.gateway;

import br.com.fiap.restaurante.domain.entity.TipoUsuario;

import java.util.Optional;

/**
 * Acesso aos tipos de usuário. A busca é pelo código, que nunca muda, e não pelo
 * nome, que pode ser renomeado.
 */
public interface ITipoUsuarioGateway {

    Optional<TipoUsuario> buscarPorCodigo(String codigo);
}
