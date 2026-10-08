package br.com.fiap.restaurante.application.gateway;

import br.com.fiap.restaurante.domain.entity.TipoUsuario;

import java.util.Optional;

public interface ITipoUsuarioGateway {

    Optional<TipoUsuario> buscarPorCodigo(String codigo);
}
