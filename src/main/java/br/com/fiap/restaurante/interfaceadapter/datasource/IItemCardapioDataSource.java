package br.com.fiap.restaurante.interfaceadapter.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;

import java.util.Optional;

/**
 * Origem de dados de itens do cardápio, implementada na infraestrutura. As buscas
 * consideram só itens ativos; a remoção é lógica.
 */
public interface IItemCardapioDataSource {

    DadosItemCardapio incluir(DadosItemCardapio item);

    DadosItemCardapio atualizar(DadosItemCardapio item);

    Optional<DadosItemCardapio> buscarPorId(Long id);

    /** {@code apenasNoLocal} nulo não filtra. */
    Pagina<DadosItemCardapio> listar(Long restauranteId, Boolean apenasNoLocal, PedidoDePagina pedido);

    boolean existeNomeAtivo(Long restauranteId, String nome);

    boolean existeNomeAtivoEmOutroItem(Long restauranteId, String nome, Long itemId);

    void remover(Long id);
}
