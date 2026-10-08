package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.util.List;

/**
 * Todos os turnos de um restaurante. Exige ao menos um turno e recusa
 * sobreposição, inclusive da parte depois da meia-noite e na passagem de domingo
 * para segunda. Dia sem turno é dia fechado. Os turnos ficam ordenados por dia e
 * por abertura.
 */
public record QuadroDeHorarios(List<Turno> turnos) {

    public QuadroDeHorarios {
        if (turnos == null || turnos.isEmpty()) {
            throw new ValidacaoDeDominioException("Informe ao menos um turno de funcionamento.");
        }
        turnos = turnos.stream().sorted().toList();
        for (int i = 0; i < turnos.size(); i++) {
            for (int j = i + 1; j < turnos.size(); j++) {
                if (turnos.get(i).sobrepoe(turnos.get(j))) {
                    throw new RegraDeNegocioException(
                            "Os turnos " + turnos.get(i) + " e " + turnos.get(j) + " se sobrepõem.");
                }
            }
        }
    }
}
