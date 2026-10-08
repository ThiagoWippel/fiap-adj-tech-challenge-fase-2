package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.infrastructure.persistence.entity.EnderecoEmbeddable;
import br.com.fiap.restaurante.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.restaurante.infrastructure.persistence.repository.TipoUsuarioRepository;
import br.com.fiap.restaurante.infrastructure.persistence.repository.UsuarioRepository;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Origem de dados de usuários com Spring Data JPA. As gravações rodam dentro da
 * transação aberta pelo caso de uso.
 */
@Component
public class JpaUsuarioDataSource implements IUsuarioDataSource {

    private final UsuarioRepository usuarios;
    private final TipoUsuarioRepository tipos;

    public JpaUsuarioDataSource(UsuarioRepository usuarios, TipoUsuarioRepository tipos) {
        this.usuarios = usuarios;
        this.tipos = tipos;
    }

    @Override
    public DadosUsuario incluir(DadosUsuario dados) {
        UsuarioEntity entidade = new UsuarioEntity();
        copiar(dados, entidade);
        return paraDados(usuarios.save(entidade));
    }

    // Altera a entidade carregada do banco. Uma entidade nova com o mesmo id
    // chegaria sem a data de criação e a devolveria nula.
    @Override
    public DadosUsuario atualizar(DadosUsuario dados) {
        UsuarioEntity entidade = usuarios.findById(dados.id()).orElseThrow();
        copiar(dados, entidade);
        // O flush dispara a auditoria, que preenche a data da última alteração.
        return paraDados(usuarios.saveAndFlush(entidade));
    }

    @Override
    public Optional<DadosUsuario> buscarPorId(Long id) {
        return usuarios.findByIdAndRemovidoEmIsNull(id).map(JpaUsuarioDataSource::paraDados);
    }

    @Override
    public Optional<DadosUsuario> buscarPorLogin(String login) {
        return usuarios.findByLoginAndRemovidoEmIsNull(login).map(JpaUsuarioDataSource::paraDados);
    }

    @Override
    public List<DadosUsuario> buscarPorNome(String nome) {
        return usuarios.findByNomeContainingAndRemovidoEmIsNullOrderByNomeAsc(nome).stream()
                .map(JpaUsuarioDataSource::paraDados)
                .toList();
    }

    @Override
    public Pagina<DadosUsuario> buscarPorNome(String nome, PedidoDePagina pedido) {
        var pagina = usuarios.findByNomeContainingAndRemovidoEmIsNull(nome, Paginas.paraPageRequest(pedido));
        return Paginas.paraPagina(pagina, JpaUsuarioDataSource::paraDados);
    }

    @Override
    public Pagina<DadosUsuario> buscarPorTipo(Long tipoId, PedidoDePagina pedido) {
        return Paginas.paraPagina(usuarios.findByTipoIdAndRemovidoEmIsNull(tipoId, Paginas.paraPageRequest(pedido)),
                JpaUsuarioDataSource::paraDados);
    }

    @Override
    public long contarAtivosPorTipo(Long tipoId) {
        return usuarios.countByTipoIdAndRemovidoEmIsNull(tipoId);
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarios.existsByEmail(email);
    }

    @Override
    public boolean existeEmailEmOutroUsuario(String email, Long id) {
        return usuarios.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existeLogin(String login) {
        return usuarios.existsByLogin(login);
    }

    @Override
    public boolean existeLoginEmOutroUsuario(String login, Long id) {
        return usuarios.existsByLoginAndIdNot(login, id);
    }

    @Override
    public boolean existeDocumento(String documento) {
        return usuarios.existsByDocumento(documento);
    }

    @Override
    public boolean existeDocumentoEmOutroUsuario(String documento, Long id) {
        return usuarios.existsByDocumentoAndIdNot(documento, id);
    }

    @Override
    public void anonimizar(Long id) {
        UsuarioEntity entidade = usuarios.findById(id).orElseThrow();
        entidade.anonimizar(LocalDateTime.now());
        usuarios.save(entidade);
    }

    private void copiar(DadosUsuario dados, UsuarioEntity entidade) {
        DadosEndereco endereco = dados.endereco();
        entidade.setNome(dados.nome());
        entidade.setEmail(dados.email());
        entidade.setLogin(dados.login());
        entidade.setSenha(dados.senha());
        entidade.setDocumento(dados.documento());
        entidade.setTipo(tipos.findById(dados.tipo().id()).orElseThrow());
        entidade.setEndereco(new EnderecoEmbeddable(endereco.rua(), endereco.numero(), endereco.complemento(),
                endereco.bairro(), endereco.cidade(), endereco.estado(), endereco.cep()));
    }

    static DadosUsuario paraDados(UsuarioEntity entidade) {
        EnderecoEmbeddable endereco = entidade.getEndereco();
        return new DadosUsuario(entidade.getId(), entidade.getNome(), entidade.getEmail(), entidade.getLogin(),
                entidade.getSenha(), entidade.getDocumento(), JpaTipoUsuarioDataSource.paraDados(entidade.getTipo()),
                new DadosEndereco(endereco.getRua(), endereco.getNumero(), endereco.getComplemento(),
                        endereco.getBairro(), endereco.getCidade(), endereco.getEstado(), endereco.getCep()),
                entidade.getDataCriacao(), entidade.getDataUltimaAlteracao());
    }
}
