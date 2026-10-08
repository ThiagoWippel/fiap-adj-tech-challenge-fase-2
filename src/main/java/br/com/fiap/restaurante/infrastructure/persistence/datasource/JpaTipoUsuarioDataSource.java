package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.infrastructure.persistence.entity.TipoUsuarioEntity;
import br.com.fiap.restaurante.infrastructure.persistence.repository.TipoUsuarioRepository;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaTipoUsuarioDataSource implements ITipoUsuarioDataSource {

    private final TipoUsuarioRepository repository;

    public JpaTipoUsuarioDataSource(TipoUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<DadosTipoUsuario> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(JpaTipoUsuarioDataSource::paraDados);
    }

    static DadosTipoUsuario paraDados(TipoUsuarioEntity entidade) {
        return new DadosTipoUsuario(entidade.getId(), entidade.getNome(), entidade.getCodigo());
    }
}
