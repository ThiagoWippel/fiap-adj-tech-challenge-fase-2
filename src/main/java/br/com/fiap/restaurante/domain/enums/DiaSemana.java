package br.com.fiap.restaurante.domain.enums;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Dia da semana dos turnos, de segunda a domingo. A ordem das constantes é a
 * ordem da semana e é usada no cálculo de sobreposição dos turnos.
 */
public enum DiaSemana {

    SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO;

    /** Converte o texto recebido, sem diferenciar maiúsculas. */
    public static DiaSemana de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoDeDominioException("O dia da semana do turno é obrigatório.");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(dia -> dia.name().equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new ValidacaoDeDominioException("O dia " + valor.trim()
                        + " não existe. Valores aceitos: "
                        + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + "."));
    }
}
