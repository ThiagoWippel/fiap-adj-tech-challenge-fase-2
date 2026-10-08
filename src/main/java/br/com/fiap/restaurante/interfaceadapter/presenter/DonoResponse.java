package br.com.fiap.restaurante.interfaceadapter.presenter;

/**
 * Dono do restaurante na resposta da API: só o id e o nome.
 */
public record DonoResponse(Long id, String nome) {
}
