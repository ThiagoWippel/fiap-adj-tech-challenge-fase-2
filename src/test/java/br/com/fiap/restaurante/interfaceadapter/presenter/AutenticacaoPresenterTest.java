package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Autenticacao;
import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Presenter de login")
class AutenticacaoPresenterTest {

    @Test
    @DisplayName("LOG-01 · a resposta do login traz id, nome, tipo, token e validade, sem dados de perfil")
    void deveMontarARespostaDoLogin() {
        /* arrange */
        TokenDeAcesso token = new TokenDeAcesso("eyJhbGciOiJIUzI1NiJ9.exemplo.assinatura",
                LocalDateTime.of(2026, 10, 8, 11, 0, 0, 999_000));

        /* act */
        LoginResponse resposta = AutenticacaoPresenter.paraResposta(new Autenticacao(maria(), token));

        /* assert */
        assertThat(resposta).isEqualTo(new LoginResponse(7L, "Maria Silva", "CLIENTE",
                "eyJhbGciOiJIUzI1NiJ9.exemplo.assinatura", LocalDateTime.of(2026, 10, 8, 11, 0, 0)));
    }
}
