package br.com.fiap.restaurante.interfaceadapter.datasource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Item do cardápio no formato trocado com a origem de dados. As datas são
 * preenchidas pela origem de dados e ignoradas na gravação.
 */
public record DadosItemCardapio(Long id, Long restauranteId, String nome, String descricao, BigDecimal preco,
                                Boolean apenasNoLocal, String caminhoFoto, LocalDateTime dataCriacao,
                                LocalDateTime dataUltimaAlteracao) {
}
