package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;

import java.util.Optional;

/**
 * Gateway de tipos de usuário: converte entre a entidade de domínio e os dados da
 * origem de dados.
 */
public class TipoUsuarioGateway implements ITipoUsuarioGateway {

    private final ITipoUsuarioDataSource dataSource;

    private TipoUsuarioGateway(ITipoUsuarioDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static TipoUsuarioGateway create(ITipoUsuarioDataSource dataSource) {
        return new TipoUsuarioGateway(dataSource);
    }

    @Override
    public TipoUsuario incluir(TipoUsuario tipo) {
        return paraTipo(dataSource.incluir(paraDados(tipo)));
    }

    @Override
    public TipoUsuario atualizar(TipoUsuario tipo) {
        return paraTipo(dataSource.atualizar(paraDados(tipo)));
    }

    @Override
    public void excluir(Long id) {
        dataSource.excluir(id);
    }

    @Override
    public Optional<TipoUsuario> buscarPorId(Long id) {
        return dataSource.buscarPorId(id).map(TipoUsuarioGateway::paraTipo);
    }

    @Override
    public Optional<TipoUsuario> buscarPorIdParaAlterar(Long id) {
        return dataSource.buscarPorIdParaAlterar(id).map(TipoUsuarioGateway::paraTipo);
    }

    @Override
    public Optional<TipoUsuario> buscarPorCodigo(String codigo) {
        return dataSource.buscarPorCodigo(codigo).map(TipoUsuarioGateway::paraTipo);
    }

    @Override
    public Pagina<TipoUsuario> listar(PedidoDePagina pedido) {
        return dataSource.listar(pedido).map(TipoUsuarioGateway::paraTipo);
    }

    @Override
    public boolean existeNome(String nome) {
        return dataSource.existeNome(nome);
    }

    @Override
    public boolean existeNomeEmOutroTipo(String nome, Long id) {
        return dataSource.existeNomeEmOutroTipo(nome, id);
    }

    static TipoUsuario paraTipo(DadosTipoUsuario dados) {
        return TipoUsuario.create(dados.id(), dados.nome(), dados.codigo());
    }

    static DadosTipoUsuario paraDados(TipoUsuario tipo) {
        return new DadosTipoUsuario(tipo.getId(), tipo.getNome(), tipo.getCodigo());
    }
}
