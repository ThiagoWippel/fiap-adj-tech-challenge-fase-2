package br.com.fiap.restaurante.interfaceadapter.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;

import java.util.Optional;

/**
 * Origem de dados de restaurantes, implementada na infraestrutura. As buscas e a
 * contagem consideram só restaurantes ativos; a remoção é lógica.
 */
public interface IRestauranteDataSource {

    DadosRestaurante incluir(DadosRestaurante restaurante);

    /** Grava os dados e substitui todos os turnos. */
    DadosRestaurante atualizar(DadosRestaurante restaurante);

    Optional<DadosRestaurante> buscarPorId(Long id);

    /**
     * Como {@link #buscarPorId}, mas reserva o registro até o fim da transação:
     * outra operação que queira alterá-lo espera esta terminar.
     */
    Optional<DadosRestaurante> buscarPorIdParaAlterar(Long id);

    /** {@code tipoCozinha} nulo não filtra. */
    Pagina<DadosRestaurante> listar(String nome, String tipoCozinha, PedidoDePagina pedido);

    Pagina<DadosRestaurante> buscarPorDono(Long donoId, PedidoDePagina pedido);

    long contarAtivosPorDono(Long donoId);

    boolean existeAtivo(Long id);

    void remover(Long id);
}
