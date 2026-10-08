package br.com.fiap.restaurante.domain.enums;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dia da semana")
class DiaSemanaTest {

    @Test
    @DisplayName("HOR-01 · os dias vão de segunda a domingo, em português")
    void deveTerOsDiasEmPortugues() {
        /* act + assert */
        assertThat(DiaSemana.values()).containsExactly(DiaSemana.SEGUNDA, DiaSemana.TERCA, DiaSemana.QUARTA,
                DiaSemana.QUINTA, DiaSemana.SEXTA, DiaSemana.SABADO, DiaSemana.DOMINGO);
        assertThat(DiaSemana.de(" sexta ")).isEqualTo(DiaSemana.SEXTA);
    }

    @Test
    @DisplayName("HOR-13 · dia inexistente ou ausente é recusado, com os valores aceitos na mensagem")
    void deveRecusarDiaInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> DiaSemana.de("FERIADO"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O dia FERIADO não existe. Valores aceitos: SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO.");
        assertThatThrownBy(() -> DiaSemana.de(null))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O dia da semana do turno é obrigatório.");
        assertThatThrownBy(() -> DiaSemana.de("  "))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O dia da semana do turno é obrigatório.");
    }
}
