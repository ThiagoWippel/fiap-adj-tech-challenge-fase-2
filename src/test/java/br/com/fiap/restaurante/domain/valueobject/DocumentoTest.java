package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Documento")
class DocumentoTest {

    @Test
    @DisplayName("DOC-01 · CPF válido é aceito, com ou sem máscara, e guardado só com dígitos")
    void deveAceitarCpfValido() {
        /* act */
        Documento semMascara = Documento.cpf("12345678909");
        Documento comMascara = Documento.cpf("123.456.789-09");

        /* assert */
        assertThat(semMascara.numero()).isEqualTo("12345678909");
        assertThat(comMascara).isEqualTo(semMascara);
        assertThat(comMascara.ehCpf()).isTrue();
        assertThat(comMascara.ehCnpj()).isFalse();
    }

    @ParameterizedTest(name = "DOC-02 · CPF \"{0}\" é recusado")
    @ValueSource(strings = {"12345678900", "12345678919", "11111111111", "1234567890", "123456789012", "123.456.789-0A"})
    void deveRecusarCpfInvalido(String cpf) {
        /* act + assert */
        assertThatThrownBy(() -> Documento.cpf(cpf))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O CPF informado não é válido.");
    }

    @Test
    @DisplayName("DOC-03 · CNPJ válido é aceito, com ou sem máscara, e guardado só com dígitos")
    void deveAceitarCnpjValido() {
        /* act */
        Documento semMascara = Documento.cnpj("11222333000181");
        Documento comMascara = Documento.cnpj("11.222.333/0001-81");

        /* assert */
        assertThat(semMascara.numero()).isEqualTo("11222333000181");
        assertThat(comMascara).isEqualTo(semMascara);
        assertThat(comMascara.ehCnpj()).isTrue();
        assertThat(comMascara.ehCpf()).isFalse();
        assertThat(Documento.cnpj("11.222.333/0005-05").numero()).isEqualTo("11222333000505");
    }

    @ParameterizedTest(name = "DOC-04 · CNPJ \"{0}\" é recusado")
    @ValueSource(strings = {"11222333000180", "11222333000191", "22222222222222", "1122233300018", "11.222.333/0001-8X"})
    void deveRecusarCnpjInvalido(String cnpj) {
        /* act + assert */
        assertThatThrownBy(() -> Documento.cnpj(cnpj))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O CNPJ informado não é válido.");
    }

    @ParameterizedTest(name = "DOC-02 · documento \"{0}\" ausente é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRecusarDocumentoAusente(String valor) {
        /* act + assert */
        assertThatThrownBy(() -> Documento.cpf(valor)).hasMessage("O CPF informado não é válido.");
        assertThatThrownBy(() -> Documento.cnpj(valor)).hasMessage("O CNPJ informado não é válido.");
    }

    @Test
    @DisplayName("DOC-01 · ao ler do banco, o tipo do documento vem da quantidade de dígitos")
    void deveIdentificarOTipoPelaQuantidadeDeDigitos() {
        /* act + assert */
        assertThat(Documento.de("12345678909").ehCpf()).isTrue();
        assertThat(Documento.de("11222333000181").ehCnpj()).isTrue();
        assertThatThrownBy(() -> Documento.de("123"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O documento deve ser um CPF ou um CNPJ.");
        assertThatThrownBy(() -> Documento.de(null))
                .hasMessage("O documento deve ser um CPF ou um CNPJ.");
    }
}
