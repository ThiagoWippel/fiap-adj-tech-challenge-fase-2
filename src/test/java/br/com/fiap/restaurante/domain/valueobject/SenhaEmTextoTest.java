package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Senha em texto")
class SenhaEmTextoTest {

    @Test
    @DisplayName("USU-26 · senhas de 8 a 72 caracteres são aceitas")
    void deveAceitarSenhaDentroDoLimite() {
        /* act + assert */
        assertThat(new SenhaEmTexto("12345678").valor()).isEqualTo("12345678");
        assertThat(new SenhaEmTexto("a".repeat(72)).valor()).hasSize(72);
    }

    @ParameterizedTest(name = "USU-26 · senha \"{0}\" é recusada")
    @NullAndEmptySource
    @ValueSource(strings = {"1234567", "        "})
    void deveRecusarSenhaCurtaOuAusente(String senha) {
        /* act + assert */
        assertThatThrownBy(() -> new SenhaEmTexto(senha))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A senha deve ter entre 8 e 72 caracteres.");
    }

    @Test
    @DisplayName("USU-26 · senha com mais de 72 caracteres é recusada")
    void deveRecusarSenhaLongaDemais() {
        /* act + assert */
        assertThatThrownBy(() -> new SenhaEmTexto("a".repeat(73)))
                .hasMessage("A senha deve ter entre 8 e 72 caracteres.");
    }

    @Test
    @DisplayName("USU-31 · senha com até 72 caracteres, mas acima de 72 bytes, é recusada")
    void deveRecusarSenhaAcimaDe72Bytes() {
        /* arrange */
        String acentuada = "é".repeat(37); // 37 caracteres, 74 bytes

        /* act + assert */
        assertThatThrownBy(() -> new SenhaEmTexto(acentuada))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A senha deve ter no máximo 72 bytes. Letras acentuadas contam como dois.");
    }

    @Test
    @DisplayName("USU-31 · senha acentuada com exatamente 72 bytes é aceita")
    void deveAceitarSenhaAcentuadaNoLimite() {
        /* act + assert */
        assertThat(new SenhaEmTexto("é".repeat(36)).valor()).hasSize(36);
    }

    @Test
    @DisplayName("USU-29 · a senha não aparece no toString")
    void naoDeveExporASenhaNoToString() {
        /* act + assert */
        assertThat(new SenhaEmTexto("SenhaSegura123")).hasToString("SenhaEmTexto[****]");
    }
}
