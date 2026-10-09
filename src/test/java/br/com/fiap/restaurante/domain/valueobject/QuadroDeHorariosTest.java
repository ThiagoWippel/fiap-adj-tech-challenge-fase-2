package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.enums.DiaSemana;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.fiap.restaurante.domain.enums.DiaSemana.DOMINGO;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SABADO;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SEGUNDA;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SEXTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quadro de horários")
class QuadroDeHorariosTest {

    private static Turno turno(DiaSemana dia, int abre, int fecha) {
        return new Turno(dia, LocalTime.of(abre, 0), LocalTime.of(fecha, 0));
    }

    @Test
    @DisplayName("HOR-04 · HOR-11 · turnos sem sobreposição são aceitos e ficam ordenados por dia e abertura")
    void deveAceitarEOrdenarTurnos() {
        /* act */
        QuadroDeHorarios quadro = new QuadroDeHorarios(List.of(turno(SEXTA, 18, 2), turno(SEGUNDA, 18, 23),
                turno(SEGUNDA, 11, 15)));

        /* assert */
        assertThat(quadro.turnos()).extracting(Turno::toString)
                .containsExactly("SEGUNDA 11:00–15:00", "SEGUNDA 18:00–23:00", "SEXTA 18:00–02:00");
    }

    @Test
    @DisplayName("HOR-07 · turnos que encostam são aceitos")
    void deveAceitarTurnosQueEncostam() {
        /* act */
        QuadroDeHorarios quadro = new QuadroDeHorarios(List.of(turno(SEGUNDA, 11, 15), turno(SEGUNDA, 15, 18)));

        /* assert */
        assertThat(quadro.turnos()).hasSize(2);
    }

    @Test
    @DisplayName("HOR-05 · sobreposição parcial é recusada, com uma mensagem que cita os dois turnos")
    void deveRecusarSobreposicaoParcial() {
        /* act + assert */
        assertThatThrownBy(() -> new QuadroDeHorarios(List.of(turno(SEGUNDA, 11, 15), turno(SEGUNDA, 14, 18))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Os turnos SEGUNDA 11:00–15:00 e SEGUNDA 14:00–18:00 se sobrepõem.");
    }

    @Test
    @DisplayName("HOR-06 · turno contido em outro é recusado")
    void deveRecusarTurnoContido() {
        /* act + assert */
        assertThatThrownBy(() -> new QuadroDeHorarios(List.of(turno(SEGUNDA, 9, 18), turno(SEGUNDA, 12, 13))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Os turnos SEGUNDA 09:00–18:00 e SEGUNDA 12:00–13:00 se sobrepõem.");
    }

    @Test
    @DisplayName("HOR-08 · turno da madrugada que colide com a noite anterior é recusado")
    void deveRecusarColisaoDepoisDaMeiaNoite() {
        /* act + assert */
        assertThatThrownBy(() -> new QuadroDeHorarios(List.of(turno(SABADO, 1, 10), turno(SEXTA, 18, 2))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Os turnos SEXTA 18:00–02:00 e SABADO 01:00–10:00 se sobrepõem.");
    }

    @Test
    @DisplayName("HOR-09 · domingo à noite que colide com segunda de madrugada é recusado")
    void deveRecusarColisaoNaVoltaDaSemana() {
        /* act + assert */
        assertThatThrownBy(() -> new QuadroDeHorarios(List.of(turno(DOMINGO, 22, 3), turno(SEGUNDA, 2, 6))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Os turnos SEGUNDA 02:00–06:00 e DOMINGO 22:00–03:00 se sobrepõem.");
    }

    @ParameterizedTest(name = "HOR-10 · lista de turnos {0} é recusada")
    @NullAndEmptySource
    void deveRecusarListaVazia(List<Turno> turnos) {
        /* act + assert */
        assertThatThrownBy(() -> new QuadroDeHorarios(turnos))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("Informe ao menos um turno de funcionamento.");
    }

    @Test
    @DisplayName("HOR-15 · até 50 turnos são aceitos; o 51º é recusado")
    void deveLimitarAQuantidadeDeTurnos() {
        /* arrange */
        List<Turno> cinquenta = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            // Turnos de uma hora espalhados pela semana, sem sobreposição
            cinquenta.add(turno(DiaSemana.values()[i % 7], i / 7, i / 7 + 1));
        }
        List<Turno> cinquentaEUm = new ArrayList<>(cinquenta);
        cinquentaEUm.add(turno(DOMINGO, 20, 21));

        /* act + assert */
        assertThat(new QuadroDeHorarios(cinquenta).turnos()).hasSize(50);
        assertThatThrownBy(() -> new QuadroDeHorarios(cinquentaEUm))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("Informe no máximo 50 turnos de funcionamento.");
    }

    @Test
    @DisplayName("HOR-10 · a lista guardada não pode ser alterada por fora")
    void deveGuardarUmaCopiaImutavel() {
        /* arrange */
        ArrayList<Turno> turnos = new ArrayList<>(List.of(turno(SEGUNDA, 11, 15)));
        QuadroDeHorarios quadro = new QuadroDeHorarios(turnos);

        /* act */
        turnos.add(turno(SEGUNDA, 18, 23));

        /* assert */
        assertThat(quadro.turnos()).hasSize(1);
        assertThatThrownBy(() -> quadro.turnos().add(turno(SEXTA, 18, 23)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
