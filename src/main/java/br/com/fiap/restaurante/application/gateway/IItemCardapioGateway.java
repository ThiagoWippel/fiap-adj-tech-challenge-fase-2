package br.com.fiap.restaurante.application.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;

import java.util.Optional;

/**
 * Acesso aos itens do cardápio. As buscas consideram só itens ativos; o item
 * excluído continua no banco, marcado como removido. As verificações de nome não
 * diferenciam maiúsculas, acentos nem espaço no fim.
 */
public interface IItemCardapioGateway {

    ItemCardapio incluir(ItemCardapio item);

    ItemCardapio atualizar(ItemCardapio item);

    Optional<ItemCardapio> buscarPorId(Long id);

    /**
     * Como {@link #buscarPorId}, mas reserva o registro até o fim da transação:
     * outra operação que queira alterá-lo espera esta terminar.
     */
    Optional<ItemCardapio> buscarPorIdParaAlterar(Long id);

    /** {@code apenasNoLocal} nulo não filtra. */
    Pagina<ItemCardapio> listar(Long restauranteId, Boolean apenasNoLocal, PedidoDePagina pedido);

    boolean existeNomeAtivo(Long restauranteId, String nome);

    boolean existeNomeAtivoEmOutroItem(Long restauranteId, String nome, Long itemId);

    void remover(Long id);
}
