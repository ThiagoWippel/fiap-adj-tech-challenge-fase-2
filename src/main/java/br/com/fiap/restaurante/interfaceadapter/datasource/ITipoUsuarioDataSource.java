package br.com.fiap.restaurante.interfaceadapter.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;

import java.util.Optional;

/**
 * Origem de dados de tipos de usuário, implementada na infraestrutura. A
 * atualização grava só o nome; o código nunca muda.
 */
public interface ITipoUsuarioDataSource {

    DadosTipoUsuario incluir(DadosTipoUsuario tipo);

    DadosTipoUsuario atualizar(DadosTipoUsuario tipo);

    void excluir(Long id);

    Optional<DadosTipoUsuario> buscarPorId(Long id);

    /**
     * Como {@link #buscarPorId}, mas reserva o registro até o fim da transação:
     * outra operação que queira alterá-lo espera esta terminar.
     */
    Optional<DadosTipoUsuario> buscarPorIdParaAlterar(Long id);

    Optional<DadosTipoUsuario> buscarPorCodigo(String codigo);

    Pagina<DadosTipoUsuario> listar(PedidoDePagina pedido);

    boolean existeNome(String nome);

    boolean existeNomeEmOutroTipo(String nome, Long id);
}
