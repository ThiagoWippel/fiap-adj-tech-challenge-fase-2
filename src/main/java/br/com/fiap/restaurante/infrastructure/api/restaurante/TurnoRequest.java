package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.application.dto.TurnoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Turno recebido pela API. Fechamento antes da abertura quer dizer que o turno
 * termina no dia seguinte.
 */
public record TurnoRequest(
        @Schema(example = "SEXTA", description = "SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO ou DOMINGO")
        @NotBlank(message = "O dia da semana do turno é obrigatório.")
        String diaSemana,

        @Schema(example = "18:00")
        @NotBlank(message = "A abertura do turno é obrigatória.")
        @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "O horário deve estar no formato HH:mm, como 18:30.")
        String abertura,

        @Schema(example = "02:00")
        @NotBlank(message = "O fechamento do turno é obrigatório.")
        @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "O horário deve estar no formato HH:mm, como 18:30.")
        String fechamento) {

    public TurnoDTO paraDTO() {
        return new TurnoDTO(diaSemana, abertura, fechamento);
    }
}
