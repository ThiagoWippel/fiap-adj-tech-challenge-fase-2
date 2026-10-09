package br.com.fiap.restaurante.interfaceadapter.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;

import java.util.List;
import java.util.Optional;

/**
 * Origem de dados de usuários, implementada na infraestrutura. As buscas
 * consideram só usuários ativos.
 */
public interface IUsuarioDataSource {

    DadosUsuario incluir(DadosUsuario usuario);

    DadosUsuario atualizar(DadosUsuario usuario);

    Optional<DadosUsuario> buscarPorId(Long id);

    /**
     * Como {@link #buscarPorId}, mas reserva o registro até o fim da transação:
     * outra operação que queira alterá-lo espera esta terminar.
     */
    Optional<DadosUsuario> buscarPorIdParaAlterar(Long id);

    Optional<DadosUsuario> buscarPorLogin(String login);

    List<DadosUsuario> buscarPorNome(String nome);

    Pagina<DadosUsuario> buscarPorNome(String nome, PedidoDePagina pedido);

    boolean existeEmail(String email);

    boolean existeEmailEmOutroUsuario(String email, Long id);

    boolean existeLogin(String login);

    boolean existeLoginEmOutroUsuario(String login, Long id);

    boolean existeDocumento(String documento);

    boolean existeDocumentoEmOutroUsuario(String documento, Long id);

    long contarAtivosPorTipo(Long tipoId);

    Pagina<DadosUsuario> buscarPorTipo(Long tipoId, PedidoDePagina pedido);

    void anonimizar(Long id);
}
