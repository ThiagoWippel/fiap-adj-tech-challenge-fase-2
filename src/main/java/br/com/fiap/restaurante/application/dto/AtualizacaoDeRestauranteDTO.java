package br.com.fiap.restaurante.application.dto;

import java.util.List;

/**
 * Dados da atualização de restaurante. Os turnos substituem os anteriores, e um
 * {@code donoId} diferente transfere o restaurante.
 */
public record AtualizacaoDeRestauranteDTO(Long id, String nome, EnderecoDTO endereco, String tipoCozinha,
                                          List<TurnoDTO> horarios, Long donoId) {
}
