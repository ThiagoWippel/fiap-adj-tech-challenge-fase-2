package br.com.fiap.restaurante.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exceções de domínio")
class ExcecoesDeDominioTest {

    @Test
    @DisplayName("ERR-08 · ValidacaoDeDominioException é uma IllegalArgumentException, como no material do curso")
    void validacaoDeDominioDeveSerIllegalArgumentException() {
        /* arrange */
        String mensagem = "O nome deve ter entre 3 e 120 caracteres.";

        /* act */
        ValidacaoDeDominioException excecao = new ValidacaoDeDominioException(mensagem);

        /* assert */
        assertThat(excecao)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(mensagem);
    }

    @Test
    @DisplayName("ERR-01 · RegraDeNegocioException carrega a mensagem que vira o detail da resposta")
    void regraDeNegocioDeveCarregarAMensagem() {
        /* arrange */
        String mensagem = "O CPF é obrigatório para usuários do tipo Cliente.";

        /* act */
        RegraDeNegocioException excecao = new RegraDeNegocioException(mensagem);

        /* assert */
        assertThat(excecao)
                .isInstanceOf(RuntimeException.class)
                .isNotInstanceOf(IllegalArgumentException.class)
                .hasMessage(mensagem);
    }
}
