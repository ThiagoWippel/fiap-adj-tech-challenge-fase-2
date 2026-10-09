package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.infrastructure.persistence.entity.TipoUsuarioEntity;
import br.com.fiap.restaurante.infrastructure.persistence.repository.TipoUsuarioRepository;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Origem de dados de tipos de usuário com Spring Data JPA.
 */
@Component
public class JpaTipoUsuarioDataSource implements ITipoUsuarioDataSource {

    private final TipoUsuarioRepository repository;

    public JpaTipoUsuarioDataSource(TipoUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public DadosTipoUsuario incluir(DadosTipoUsuario dados) {
        TipoUsuarioEntity entidade = new TipoUsuarioEntity();
        entidade.setNome(dados.nome());
        entidade.setCodigo(dados.codigo());
        return paraDados(repository.saveAndFlush(entidade));
    }

    // Só o nome muda; a coluna do código nem entra no UPDATE.
    @Override
    public DadosTipoUsuario atualizar(DadosTipoUsuario dados) {
        TipoUsuarioEntity entidade = repository.findById(dados.id()).orElseThrow();
        entidade.setNome(dados.nome());
        return paraDados(repository.saveAndFlush(entidade));
    }

    // O flush faz a chave estrangeira barrar a exclusão aqui, e não só no commit.
    @Override
    public void excluir(Long id) {
        repository.deleteById(id);
        repository.flush();
    }

    @Override
    public Optional<DadosTipoUsuario> buscarPorId(Long id) {
        return repository.findById(id).map(JpaTipoUsuarioDataSource::paraDados);
    }

    // Roda na transação do caso de uso, e a reserva vale até ela terminar
    @Override
    @Transactional
    public Optional<DadosTipoUsuario> buscarPorIdParaAlterar(Long id) {
        return repository.findParaAlterarById(id).map(JpaTipoUsuarioDataSource::paraDados);
    }

    @Override
    public Optional<DadosTipoUsuario> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(JpaTipoUsuarioDataSource::paraDados);
    }

    @Override
    public Pagina<DadosTipoUsuario> listar(PedidoDePagina pedido) {
        return Paginas.paraPagina(repository.findAll(Paginas.paraPageRequest(pedido)),
                JpaTipoUsuarioDataSource::paraDados);
    }

    @Override
    public boolean existeNome(String nome) {
        return repository.existsByNome(nome);
    }

    @Override
    public boolean existeNomeEmOutroTipo(String nome, Long id) {
        return repository.existsByNomeAndIdNot(nome, id);
    }

    static DadosTipoUsuario paraDados(TipoUsuarioEntity entidade) {
        return new DadosTipoUsuario(entidade.getId(), entidade.getNome(), entidade.getCodigo());
    }
}
