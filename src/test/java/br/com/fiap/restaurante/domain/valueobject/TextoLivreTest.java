package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Texto livre")
class TextoLivreTest {

    @Test
    @DisplayName("ENT-05 · texto comum, com acento, emoji e símbolos, é aceito; nulo fica para quem chama")
    void deveAceitarTextoComum() {
        /* act + assert */
        assertThatCode(() -> TextoLivre.exigirUmaLinha("Pão de Queijo 🧀 & Café — nº 1", "nome"))
                .doesNotThrowAnyException();
        assertThatCode(() -> TextoLivre.exigirUmaLinha(null, "nome")).doesNotThrowAnyException();
        assertThatCode(() -> TextoLivre.exigirSemControle(null, "descrição")).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "ENT-05 · texto de uma linha com o caractere U+{0} é recusado")
    @ValueSource(strings = {"0000", "000A", "000D", "0009", "0007", "007F", "0085", "202E", "202A", "2066", "2069"})
    void deveRecusarControleEmTextoDeUmaLinha(String codigo) {
        /* arrange */
        String texto = "Cantina" + (char) Integer.parseInt(codigo, 16) + "da Nona";

        /* act + assert */
        assertThatThrownBy(() -> TextoLivre.exigirUmaLinha(texto, "nome"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O campo nome não aceita quebra de linha nem caracteres de controle.");
    }

    @Test
    @DisplayName("ENT-05 · texto de várias linhas aceita quebra de linha e tabulação, mas não os outros controles")
    void devePermitirQuebraDeLinhaNoTextoDeVariasLinhas() {
        /* act + assert */
        assertThatCode(() -> TextoLivre.exigirSemControle("Linha 1\nLinha 2\r\n\tLinha 3", "descrição"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> TextoLivre.exigirSemControle("Feijoada\u0000", "descrição"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O campo descrição não aceita caracteres de controle.");
        assertThatThrownBy(() -> TextoLivre.exigirSemControle("Feijoada ‮adaojief", "descrição"))
                .hasMessage("O campo descrição não aceita caracteres de controle.");
    }
}
