package br.com.fiap.restaurante.application.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Acesso aos usuários. As buscas só encontram usuários ativos: um usuário
 * excluído é anonimizado e deixa de aparecer.
 */
public interface IUsuarioGateway {

    Usuario incluir(Usuario usuario);

    Usuario atualizar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorLogin(String login);

    List<Usuario> buscarPorNome(String nome);

    Pagina<Usuario> buscarPorNome(String nome, PedidoDePagina pedido);

    boolean existeEmail(String email);

    boolean existeEmailEmOutroUsuario(String email, Long id);

    boolean existeLogin(String login);

    boolean existeLoginEmOutroUsuario(String login, Long id);

    boolean existeDocumento(String numero);

    boolean existeDocumentoEmOutroUsuario(String numero, Long id);

    long contarAtivosPorTipo(Long tipoId);

    Pagina<Usuario> buscarPorTipo(Long tipoId, PedidoDePagina pedido);

    /** Apaga os dados pessoais e mantém o registro, para preservar o histórico. */
    void anonimizar(Long id);
}
