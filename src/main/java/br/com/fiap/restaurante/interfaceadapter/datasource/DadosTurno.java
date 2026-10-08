package br.com.fiap.restaurante.interfaceadapter.datasource;

import java.time.LocalTime;

/**
 * Turno no formato trocado com a origem de dados. O dia vai como o nome da
 * constante, como SEXTA.
 */
public record DadosTurno(String diaSemana, LocalTime abertura, LocalTime fechamento) {
}
