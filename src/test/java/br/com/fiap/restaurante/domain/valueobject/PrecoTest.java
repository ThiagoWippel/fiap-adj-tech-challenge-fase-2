package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Preço")
class PrecoTest {

    @Test
    @DisplayName("ITE-07 · o preço fica sempre com duas casas: 39.9 vira 39.90")
    void deveGuardarComDuasCasas() {
        /* act + assert */
        assertThat(new Preco(new BigDecimal("39.9")).valor()).isEqualByComparingTo("39.90").hasToString("39.90");
        assertThat(new Preco(new BigDecimal("12")).valor()).hasToString("12.00");
        assertThat(new Preco(new BigDecimal("0.01")).valor()).hasToString("0.01");
    }

    @Test
    @DisplayName("ITE-07 · zeros à direita além da segunda casa não contam como casa decimal")
    void deveAceitarZerosADireita() {
        /* act + assert */
        assertThat(new Preco(new BigDecimal("39.900")).valor()).hasToString("39.90");
    }

    @ParameterizedTest(name = "ITE-04 · preço {0} é recusado")
    @ValueSource(strings = {"0", "0.00", "-1", "-0.01"})
    void deveRecusarPrecoZeroOuNegativo(String valor) {
        /* act + assert */
        assertThatThrownBy(() -> new Preco(new BigDecimal(valor)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O preço deve ser maior que zero.");
    }

    @Test
    @DisplayName("ITE-05 · preço com mais de duas casas decimais é recusado, sem arredondar")
    void deveRecusarMaisDeDuasCasas() {
        /* act + assert */
        assertThatThrownBy(() -> new Preco(new BigDecimal("39.999")))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O preço deve ter no máximo duas casas decimais.");
    }

    @Test
    @DisplayName("ITE-06 · preço acima de 99.999.999,99 é recusado; o próprio limite é aceito")
    void deveRespeitarOLimite() {
        /* act + assert */
        assertThat(new Preco(new BigDecimal("99999999.99")).valor()).hasToString("99999999.99");
        assertThatThrownBy(() -> new Preco(new BigDecimal("100000000.00")))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O preço deve ser de no máximo 99.999.999,99.");
    }

    @Test
    @DisplayName("ITE-01 · o preço é obrigatório")
    void deveExigirOPreco() {
        /* act + assert */
        assertThatThrownBy(() -> new Preco(null))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O preço é obrigatório.");
    }
}
