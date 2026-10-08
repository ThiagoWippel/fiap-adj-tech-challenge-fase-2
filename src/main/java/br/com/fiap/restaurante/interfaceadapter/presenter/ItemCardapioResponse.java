package br.com.fiap.restaurante.interfaceadapter.presenter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Item do cardápio na resposta da API. O preço sai sempre com duas casas.
 */
public record ItemCardapioResponse(Long id, Long restauranteId, String nome, String descricao, BigDecimal preco,
                                   Boolean apenasNoLocal, String caminhoFoto, LocalDateTime dataCriacao,
                                   LocalDateTime dataUltimaAlteracao) {
}
