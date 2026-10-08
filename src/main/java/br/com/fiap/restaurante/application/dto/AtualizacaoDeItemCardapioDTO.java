package br.com.fiap.restaurante.application.dto;

import java.math.BigDecimal;

/**
 * Dados da atualização de um item. O restaurante vem da rota e precisa ser o
 * dono do item.
 */
public record AtualizacaoDeItemCardapioDTO(Long restauranteId, Long itemId, String nome, String descricao,
                                           BigDecimal preco, Boolean apenasNoLocal, String caminhoFoto) {
}
