package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Endereço")
class EnderecoTest {

    @Test
    @DisplayName("END-01 · endereço completo é aceito e o complemento é opcional")
    void deveAceitarEnderecoCompleto_ComComplementoOpcional() {
        /* act */
        Endereco comComplemento = new Endereco("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301-000");
        Endereco semComplemento = new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "SC", "88301000");
        Endereco complementoEmBranco = new Endereco("Rua das Flores", "123", "  ", "Centro", "Itajaí", "SC", "88301000");

        /* assert */
        assertThat(comComplemento.complemento()).isEqualTo("Apto 45");
        assertThat(semComplemento.complemento()).isNull();
        assertThat(complementoEmBranco.complemento()).isNull();
    }

    @ParameterizedTest(name = "END-02 · o número aceita \"{0}\"")
    @ValueSource(strings = {"S/N", "123-A", "1500"})
    void deveAceitarNumeroComoTexto(String numero) {
        /* act */
        Endereco endereco = new Endereco("Rua das Flores", numero, null, "Centro", "Itajaí", "SC", "88301000");

        /* assert */
        assertThat(endereco.numero()).isEqualTo(numero);
    }

    @Test
    @DisplayName("END-03 · UF fora das 27 siglas é recusada, e a sigla é guardada em maiúsculas")
    void deveValidarAUf() {
        /* act */
        Endereco minusculas = new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "sc", "88301000");

        /* assert */
        assertThat(minusculas.estado()).isEqualTo("SC");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "XX", "88301000"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O estado deve ser a sigla de uma UF brasileira, como SC.");
    }

    @Test
    @DisplayName("END-04 · CEP com hífen é guardado só com dígitos")
    void deveGuardarOCepSoComDigitos() {
        /* act */
        Endereco endereco = new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "SC", "88301-000");

        /* assert */
        assertThat(endereco.cep()).isEqualTo("88301000");
    }

    @ParameterizedTest(name = "END-04 · CEP \"{0}\" é recusado")
    @ValueSource(strings = {"8830100", "883010000", "88301-00A", "88.301-000"})
    void deveRecusarCepInvalido(String cep) {
        /* act + assert */
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "SC", cep))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O CEP deve ter 8 dígitos.");
    }

    @ParameterizedTest(name = "END-05 · rua \"{0}\" é recusada")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRecusarRuaAusente(String rua) {
        /* act + assert */
        assertThatThrownBy(() -> new Endereco(rua, "123", null, "Centro", "Itajaí", "SC", "88301000"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O campo rua é obrigatório.");
    }

    @Test
    @DisplayName("END-05 · número, bairro, cidade, UF e CEP também são obrigatórios")
    void deveRecusarDemaisCamposAusentes() {
        /* act + assert */
        assertThatThrownBy(() -> new Endereco("Rua das Flores", null, null, "Centro", "Itajaí", "SC", "88301000"))
                .hasMessage("O campo número é obrigatório.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, " ", "Itajaí", "SC", "88301000"))
                .hasMessage("O campo bairro é obrigatório.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "Centro", "", "SC", "88301000"))
                .hasMessage("O campo cidade é obrigatório.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", null, "88301000"))
                .hasMessage("O campo estado é obrigatório.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "SC", null))
                .hasMessage("O campo CEP é obrigatório.");
    }

    @Test
    @DisplayName("END-05 · campos acima do tamanho máximo são recusados")
    void deveRecusarCamposLongosDemais() {
        /* act + assert */
        assertThatThrownBy(() -> new Endereco("R".repeat(151), "123", null, "Centro", "Itajaí", "SC", "88301000"))
                .hasMessage("O campo rua deve ter no máximo 150 caracteres.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "1".repeat(11), null, "Centro", "Itajaí", "SC", "88301000"))
                .hasMessage("O campo número deve ter no máximo 10 caracteres.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", "C".repeat(61), "Centro", "Itajaí", "SC", "88301000"))
                .hasMessage("O campo complemento deve ter no máximo 60 caracteres.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "B".repeat(81), "Itajaí", "SC", "88301000"))
                .hasMessage("O campo bairro deve ter no máximo 80 caracteres.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", null, "Centro", "C".repeat(81), "SC", "88301000"))
                .hasMessage("O campo cidade deve ter no máximo 80 caracteres.");
    }

    @Test
    @DisplayName("ENT-05 · campos do endereço com quebra de linha ou caractere de controle são recusados")
    void deveRecusarControleNoEndereco() {
        /* act + assert */
        assertThatThrownBy(() -> new Endereco("Rua das\nFlores", "123", null, "Centro", "Itajaí", "SC", "88301000"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O campo rua não aceita quebra de linha nem caracteres de controle.");
        assertThatThrownBy(() -> new Endereco("Rua das Flores", "123", "\u202EApto", "Centro", "Itajaí", "SC",
                "88301000"))
                .hasMessage("O campo complemento não aceita quebra de linha nem caracteres de controle.");
    }
}
