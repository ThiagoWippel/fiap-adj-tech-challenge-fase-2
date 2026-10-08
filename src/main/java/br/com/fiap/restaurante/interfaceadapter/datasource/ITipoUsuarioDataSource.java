package br.com.fiap.restaurante.interfaceadapter.datasource;

import java.util.Optional;

/**
 * Origem de dados de tipos de usuário, implementada na infraestrutura.
 */
public interface ITipoUsuarioDataSource {

    Optional<DadosTipoUsuario> buscarPorCodigo(String codigo);
}
