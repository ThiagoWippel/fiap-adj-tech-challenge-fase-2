package br.com.fiap.restaurante.domain.enums;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tipo de cozinha")
class TipoCozinhaTest {

    @Test
    @DisplayName("COZ-01 · aceita os valores da lista, sem diferenciar maiúsculas e ignorando espaços nas pontas")
    void deveAceitarValoresDaLista() {
        /* act + assert */
        assertThat(TipoCozinha.de("ITALIANA")).isEqualTo(TipoCozinha.ITALIANA);
        assertThat(TipoCozinha.de(" frutos_do_mar ")).isEqualTo(TipoCozinha.FRUTOS_DO_MAR);
        assertThat(TipoCozinha.values()).hasSize(18);
    }

    @Test
    @DisplayName("COZ-01 · valor fora da lista é recusado, com os valores aceitos na mensagem")
    void deveRecusarValorForaDaLista() {
        /* act + assert */
        assertThatThrownBy(() -> TipoCozinha.de("TAILANDESA"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O tipo de cozinha TAILANDESA não existe. Valores aceitos: BRASILEIRA, ITALIANA, PIZZARIA, "
                        + "JAPONESA, CHINESA, ARABE, MEXICANA, PORTUGUESA, FRANCESA, HAMBURGUERIA, LANCHES, "
                        + "CHURRASCARIA, FRUTOS_DO_MAR, VEGETARIANA, VEGANA, DOCES_E_SOBREMESAS, CAFETERIA, OUTRA.");
    }

    @ParameterizedTest(name = "COZ-02 · tipo de cozinha \"{0}\" é recusado como ausente")
    @NullAndEmptySource
    void deveRecusarValorAusente(String valor) {
        /* act + assert */
        assertThatThrownBy(() -> TipoCozinha.de(valor))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O tipo de cozinha é obrigatório.");
    }
}
