package br.com.fiap.restaurante.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Linha da tabela de restaurantes")
class RestauranteEntityTest {

    @Test
    @DisplayName("RES-08 · substituir os horários troca todos os turnos na mesma lista e liga cada um ao restaurante")
    void deveSubstituirOsHorarios() {
        /* arrange */
        RestauranteEntity restaurante = new RestauranteEntity();
        restaurante.substituirHorarios(List.of(turno("SEGUNDA", 11, 15)));
        List<HorarioFuncionamentoEntity> lista = restaurante.getHorarios();
        HorarioFuncionamentoEntity sexta = turno("SEXTA", 18, 2);
        HorarioFuncionamentoEntity sabado = turno("SABADO", 18, 23);

        /* act */
        restaurante.substituirHorarios(List.of(sexta, sabado));

        /* assert */
        // A mesma instância: é o que permite ao orphanRemoval apagar o turno que saiu
        assertThat(restaurante.getHorarios()).isSameAs(lista).containsExactly(sexta, sabado);
        assertThat(restaurante.getHorarios()).allSatisfy(horario ->
                assertThat(horario.getRestaurante()).isSameAs(restaurante));
    }

    private static HorarioFuncionamentoEntity turno(String dia, int abertura, int fechamento) {
        HorarioFuncionamentoEntity turno = new HorarioFuncionamentoEntity();
        turno.setDiaSemana(dia);
        turno.setAbertura(LocalTime.of(abertura, 0));
        turno.setFechamento(LocalTime.of(fechamento, 0));
        return turno;
    }
}
