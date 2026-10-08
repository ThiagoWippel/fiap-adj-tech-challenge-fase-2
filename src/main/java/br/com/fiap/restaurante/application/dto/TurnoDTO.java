package br.com.fiap.restaurante.application.dto;

import br.com.fiap.restaurante.domain.enums.DiaSemana;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Turno;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Turno como chega ao caso de uso: o dia e os horários em texto, no formato HH:mm.
 */
public record TurnoDTO(String diaSemana, String abertura, String fechamento) {

    public Turno paraTurno() {
        return new Turno(DiaSemana.de(diaSemana), horario(abertura), horario(fechamento));
    }

    private static LocalTime horario(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return LocalTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new ValidacaoDeDominioException(
                    "O horário " + valor + " não é válido. Use o formato HH:mm, como 18:30.");
        }
    }
}
