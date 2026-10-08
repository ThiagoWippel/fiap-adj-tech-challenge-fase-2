package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;

import java.util.List;
import java.util.Optional;

/**
 * Gateway de usuários: converte entre a entidade de domínio e os dados da origem
 * de dados. Ao reconstruir o usuário, o tipo do documento sai do número de
 * dígitos: 11 para CPF, 14 para CNPJ.
 */
public class UsuarioGateway implements IUsuarioGateway {

    private final IUsuarioDataSource dataSource;

    private UsuarioGateway(IUsuarioDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static UsuarioGateway create(IUsuarioDataSource dataSource) {
        return new UsuarioGateway(dataSource);
    }

    @Override
    public Usuario incluir(Usuario usuario) {
        return paraUsuario(dataSource.incluir(paraDados(usuario)));
    }

    @Override
    public Usuario atualizar(Usuario usuario) {
        return paraUsuario(dataSource.atualizar(paraDados(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return dataSource.buscarPorId(id).map(UsuarioGateway::paraUsuario);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return dataSource.buscarPorLogin(login).map(UsuarioGateway::paraUsuario);
    }

    @Override
    public List<Usuario> buscarPorNome(String nome) {
        return dataSource.buscarPorNome(nome).stream().map(UsuarioGateway::paraUsuario).toList();
    }

    @Override
    public Pagina<Usuario> buscarPorNome(String nome, PedidoDePagina pedido) {
        return dataSource.buscarPorNome(nome, pedido).map(UsuarioGateway::paraUsuario);
    }

    @Override
    public boolean existeEmail(String email) {
        return dataSource.existeEmail(email);
    }

    @Override
    public boolean existeEmailEmOutroUsuario(String email, Long id) {
        return dataSource.existeEmailEmOutroUsuario(email, id);
    }

    @Override
    public boolean existeLogin(String login) {
        return dataSource.existeLogin(login);
    }

    @Override
    public boolean existeLoginEmOutroUsuario(String login, Long id) {
        return dataSource.existeLoginEmOutroUsuario(login, id);
    }

    @Override
    public boolean existeDocumento(String numero) {
        return dataSource.existeDocumento(numero);
    }

    @Override
    public void anonimizar(Long id) {
        dataSource.anonimizar(id);
    }

    private static DadosUsuario paraDados(Usuario usuario) {
        Endereco endereco = usuario.getEndereco();
        return new DadosUsuario(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getLogin(),
                usuario.getSenha(), usuario.getDocumento().numero(), TipoUsuarioGateway.paraDados(usuario.getTipo()),
                new DadosEndereco(endereco.rua(), endereco.numero(), endereco.complemento(), endereco.bairro(),
                        endereco.cidade(), endereco.estado(), endereco.cep()),
                usuario.getDataCriacao(), usuario.getDataUltimaAlteracao());
    }

    private static Usuario paraUsuario(DadosUsuario dados) {
        DadosEndereco endereco = dados.endereco();
        return Usuario.create(dados.id(), dados.nome(), dados.email(), dados.login(), dados.senha(),
                new Endereco(endereco.rua(), endereco.numero(), endereco.complemento(), endereco.bairro(),
                        endereco.cidade(), endereco.estado(), endereco.cep()),
                TipoUsuarioGateway.paraTipo(dados.tipo()), Documento.de(dados.documento()),
                dados.dataCriacao(), dados.dataUltimaAlteracao());
    }
}
