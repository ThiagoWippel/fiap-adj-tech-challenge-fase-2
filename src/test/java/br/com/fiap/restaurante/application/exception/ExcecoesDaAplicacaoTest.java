package br.com.fiap.restaurante.application.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exceções da aplicação")
class ExcecoesDaAplicacaoTest {

    @Test
    @DisplayName("ERR-01 · RecursoNaoEncontradoException carrega a mensagem que vira o detail da resposta")
    void recursoNaoEncontradoDeveCarregarAMensagem() {
        /* arrange */
        String mensagem = "Usuário 7 não encontrado.";

        /* act */
        RecursoNaoEncontradoException excecao = new RecursoNaoEncontradoException(mensagem);

        /* assert */
        assertThat(excecao).isInstanceOf(RuntimeException.class).hasMessage(mensagem);
    }

    @Test
    @DisplayName("ERR-01 · ConflitoDeDadosException carrega a mensagem que vira o detail da resposta")
    void conflitoDeDadosDeveCarregarAMensagem() {
        /* arrange */
        String mensagem = "Já existe um usuário com este e-mail.";

        /* act */
        ConflitoDeDadosException excecao = new ConflitoDeDadosException(mensagem);

        /* assert */
        assertThat(excecao).isInstanceOf(RuntimeException.class).hasMessage(mensagem);
    }

    @Test
    @DisplayName("ERR-01 · CredenciaisInvalidasException carrega a mensagem que vira o detail da resposta")
    void credenciaisInvalidasDeveCarregarAMensagem() {
        /* arrange */
        String mensagem = "Login ou senha inválidos.";

        /* act */
        CredenciaisInvalidasException excecao = new CredenciaisInvalidasException(mensagem);

        /* assert */
        assertThat(excecao).isInstanceOf(RuntimeException.class).hasMessage(mensagem);
    }
}
