package br.com.fiap.restaurante.interfaceadapter.presenter;

/**
 * Turno na resposta da API, com os horários em HH:mm.
 */
public record TurnoResponse(String diaSemana, String abertura, String fechamento) {
}
