package br.com.fiap.restaurante.application.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

import java.util.Optional;

/**
 * Acesso aos tipos de usuário. As verificações de nome não diferenciam
 * maiúsculas nem acentos.
 */
public interface ITipoUsuarioGateway {

    TipoUsuario incluir(TipoUsuario tipo);

    TipoUsuario atualizar(TipoUsuario tipo);

    void excluir(Long id);

    Optional<TipoUsuario> buscarPorId(Long id);

    /**
     * Como {@link #buscarPorId}, mas reserva o registro até o fim da transação:
     * outra operação que queira alterá-lo espera esta terminar.
     */
    Optional<TipoUsuario> buscarPorIdParaAlterar(Long id);

    Optional<TipoUsuario> buscarPorCodigo(String codigo);

    Pagina<TipoUsuario> listar(PedidoDePagina pedido);

    boolean existeNome(String nome);

    boolean existeNomeEmOutroTipo(String nome, Long id);
}
