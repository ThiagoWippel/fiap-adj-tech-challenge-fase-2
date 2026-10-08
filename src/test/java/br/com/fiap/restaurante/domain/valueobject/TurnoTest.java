package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.stream.Stream;

import static br.com.fiap.restaurante.domain.enums.DiaSemana.DOMINGO;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SABADO;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SEGUNDA;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SEXTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Turno")
class TurnoTest {

    @Test
    @DisplayName("HOR-01 · turno com dia, abertura e fechamento válidos é aceito")
    void deveAceitarTurnoValido() {
        /* act */
        Turno turno = new Turno(SEGUNDA, LocalTime.of(11, 0), LocalTime.of(15, 0));

        /* assert */
        assertThat(turno.passaDaMeiaNoite()).isFalse();
        assertThat(turno).hasToString("SEGUNDA 11:00–15:00");
    }

    @Test
    @DisplayName("HOR-02 · abertura igual ao fechamento é recusada")
    void deveRecusarAberturaIgualAoFechamento() {
        /* act + assert */
        assertThatThrownBy(() -> new Turno(SEXTA, LocalTime.of(18, 0), LocalTime.of(18, 0)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O turno de SEXTA abre e fecha no mesmo horário (18:00). Para funcionar o dia todo, "
                        + "use 00:00–23:59.");
    }

    @Test
    @DisplayName("HOR-03 · fechamento antes da abertura termina no dia seguinte e é aceito")
    void deveAceitarTurnoQuePassaDaMeiaNoite() {
        /* act */
        Turno turno = new Turno(SEXTA, LocalTime.of(18, 0), LocalTime.of(2, 0));

        /* assert */
        assertThat(turno.passaDaMeiaNoite()).isTrue();
        assertThat(turno).hasToString("SEXTA 18:00–02:00");
    }

    @Test
    @DisplayName("HOR-01 · dia, abertura e fechamento são obrigatórios")
    void deveExigirTodosOsCampos() {
        /* act + assert */
        assertThatThrownBy(() -> new Turno(null, LocalTime.of(11, 0), LocalTime.of(15, 0)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O turno precisa de dia, abertura e fechamento.");
        assertThatThrownBy(() -> new Turno(SEGUNDA, null, LocalTime.of(15, 0)))
                .isInstanceOf(ValidacaoDeDominioException.class);
        assertThatThrownBy(() -> new Turno(SEGUNDA, LocalTime.of(11, 0), null))
                .isInstanceOf(ValidacaoDeDominioException.class);
    }

    @Test
    @DisplayName("HOR-04 · HOR-05 · a sobreposição considera o horário dentro do mesmo dia")
    void deveCompararTurnosDoMesmoDia() {
        /* arrange */
        Turno almoco = new Turno(SEGUNDA, LocalTime.of(11, 0), LocalTime.of(15, 0));

        /* act + assert */
        assertThat(almoco.sobrepoe(new Turno(SEGUNDA, LocalTime.of(18, 0), LocalTime.of(23, 0)))).isFalse();
        assertThat(almoco.sobrepoe(new Turno(SEGUNDA, LocalTime.of(14, 0), LocalTime.of(16, 0)))).isTrue();
        assertThat(almoco.sobrepoe(new Turno(SEGUNDA, LocalTime.of(10, 0), LocalTime.of(12, 0)))).isTrue();
        assertThat(almoco.sobrepoe(new Turno(SEXTA, LocalTime.of(11, 0), LocalTime.of(15, 0)))).isFalse();
    }

    @Test
    @DisplayName("HOR-06 · HOR-07 · turno contido em outro sobrepõe; turnos que só encostam, não")
    void deveTratarTurnoContidoETurnosQueEncostam() {
        /* arrange */
        Turno dia = new Turno(SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0));

        /* act + assert */
        assertThat(dia.sobrepoe(new Turno(SEGUNDA, LocalTime.of(12, 0), LocalTime.of(13, 0)))).isTrue();
        assertThat(new Turno(SEGUNDA, LocalTime.of(12, 0), LocalTime.of(13, 0)).sobrepoe(dia)).isTrue();
        assertThat(dia.sobrepoe(new Turno(SEGUNDA, LocalTime.of(18, 0), LocalTime.of(22, 0)))).isFalse();
        assertThat(dia.sobrepoe(new Turno(SEGUNDA, LocalTime.of(6, 0), LocalTime.of(9, 0)))).isFalse();
    }

    @Test
    @DisplayName("HOR-08 · a parte depois da meia-noite conta no dia seguinte")
    void deveConsiderarAParteDepoisDaMeiaNoite() {
        /* arrange */
        Turno sextaANoite = new Turno(SEXTA, LocalTime.of(18, 0), LocalTime.of(2, 0));

        /* act + assert */
        assertThat(sextaANoite.sobrepoe(new Turno(SABADO, LocalTime.of(1, 0), LocalTime.of(10, 0)))).isTrue();
        assertThat(sextaANoite.sobrepoe(new Turno(SABADO, LocalTime.of(2, 0), LocalTime.of(10, 0)))).isFalse();
    }

    @Test
    @DisplayName("HOR-09 · domingo à noite chega à segunda de madrugada, como num relógio que dá a volta")
    void deveDarAVoltaNaSemana() {
        /* arrange */
        Turno domingoANoite = new Turno(DOMINGO, LocalTime.of(22, 0), LocalTime.of(3, 0));
        Turno segundaCedo = new Turno(SEGUNDA, LocalTime.of(2, 0), LocalTime.of(6, 0));

        /* act + assert */
        assertThat(domingoANoite.sobrepoe(segundaCedo)).isTrue();
        assertThat(segundaCedo.sobrepoe(domingoANoite)).isTrue();
        assertThat(domingoANoite.sobrepoe(new Turno(SEGUNDA, LocalTime.of(3, 0), LocalTime.of(6, 0)))).isFalse();
    }

    @Test
    @DisplayName("HOR-11 · turnos se ordenam por dia e por horário de abertura")
    void deveOrdenarPorDiaEAbertura() {
        /* arrange */
        Turno sexta = new Turno(SEXTA, LocalTime.of(18, 0), LocalTime.of(2, 0));
        Turno segundaNoite = new Turno(SEGUNDA, LocalTime.of(18, 0), LocalTime.of(23, 0));
        Turno segundaAlmoco = new Turno(SEGUNDA, LocalTime.of(11, 0), LocalTime.of(15, 0));

        /* act + assert */
        assertThat(Stream.of(sexta, segundaNoite, segundaAlmoco).sorted().toList())
                .containsExactly(segundaAlmoco, segundaNoite, sexta);
    }
}
