package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;

import java.util.Optional;

public class TipoUsuarioGateway implements ITipoUsuarioGateway {

    private final ITipoUsuarioDataSource dataSource;

    private TipoUsuarioGateway(ITipoUsuarioDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static TipoUsuarioGateway create(ITipoUsuarioDataSource dataSource) {
        return new TipoUsuarioGateway(dataSource);
    }

    @Override
    public Optional<TipoUsuario> buscarPorCodigo(String codigo) {
        return dataSource.buscarPorCodigo(codigo).map(TipoUsuarioGateway::paraTipo);
    }

    static TipoUsuario paraTipo(DadosTipoUsuario dados) {
        return TipoUsuario.create(dados.id(), dados.nome(), dados.codigo());
    }

    static DadosTipoUsuario paraDados(TipoUsuario tipo) {
        return new DadosTipoUsuario(tipo.getId(), tipo.getNome(), tipo.getCodigo());
    }
}
