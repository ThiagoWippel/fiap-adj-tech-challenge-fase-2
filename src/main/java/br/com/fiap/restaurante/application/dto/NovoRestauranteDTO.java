package br.com.fiap.restaurante.application.dto;

import java.util.List;

/**
 * Dados do cadastro de restaurante. {@code tipoCozinha} vem em texto e
 * {@code donoId} aponta para um usuário do tipo Dono de Restaurante.
 */
public record NovoRestauranteDTO(String nome, EnderecoDTO endereco, String tipoCozinha, List<TurnoDTO> horarios,
                                 Long donoId) {
}
