package br.com.fiap.restaurante.application.dto;

import java.math.BigDecimal;

/**
 * Dados do cadastro de um item no cardápio de um restaurante.
 */
public record NovoItemCardapioDTO(Long restauranteId, String nome, String descricao, BigDecimal preco,
                                  Boolean apenasNoLocal, String caminhoFoto) {
}
