package br.com.fiap.restaurante.interfaceadapter.datasource;

import java.util.Optional;

public interface ITipoUsuarioDataSource {

    Optional<DadosTipoUsuario> buscarPorCodigo(String codigo);
}
