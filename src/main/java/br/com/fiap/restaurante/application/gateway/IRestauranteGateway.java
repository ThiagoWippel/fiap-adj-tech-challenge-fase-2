package br.com.fiap.restaurante.application.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;

import java.util.Optional;

/**
 * Acesso aos restaurantes. As buscas e a contagem consideram só restaurantes
 * ativos; o restaurante excluído continua no banco, marcado como removido.
 */
public interface IRestauranteGateway {

    Restaurante incluir(Restaurante restaurante);

    Restaurante atualizar(Restaurante restaurante);

    Optional<Restaurante> buscarPorId(Long id);

    /** {@code tipoCozinha} nulo não filtra; nome vazio também não. */
    Pagina<Restaurante> listar(String nome, TipoCozinha tipoCozinha, PedidoDePagina pedido);

    Pagina<Restaurante> buscarPorDono(Long donoId, PedidoDePagina pedido);

    long contarAtivosPorDono(Long donoId);

    void remover(Long id);
}
