package br.com.fiap.restaurante.interfaceadapter.presenter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Restaurante na resposta da API, com os turnos ordenados por dia e abertura.
 */
public record RestauranteResponse(Long id, String nome, EnderecoResponse endereco, String tipoCozinha,
                                  List<TurnoResponse> horarios, DonoResponse dono, LocalDateTime dataCriacao,
                                  LocalDateTime dataUltimaAlteracao) {
}
