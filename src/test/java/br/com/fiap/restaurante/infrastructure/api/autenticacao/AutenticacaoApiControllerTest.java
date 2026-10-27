package br.com.fiap.restaurante.infrastructure.api.autenticacao;

import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.LoginResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Endpoint de login")
class AutenticacaoApiControllerTest {

    @Test
    @DisplayName("LOG-01 · login com credenciais válidas devolve 200 com o token e a validade")
    void deveAutenticar() {
        /* arrange */
        IUsuarioDataSource usuarios = mock(IUsuarioDataSource.class);
        IPasswordEncoder senhas = mock(IPasswordEncoder.class);
        ITokenGenerator tokens = mock(ITokenGenerator.class);
        LocalDateTime expiracao = LocalDateTime.of(2026, 10, 8, 11, 0, 0);
        when(usuarios.buscarPorLogin("maria.silva")).thenReturn(Optional.of(DadosDeExemplo.maria()));
        when(senhas.confere(SENHA, SENHA_CODIFICADA)).thenReturn(true);
        when(tokens.gerar(any())).thenReturn(new TokenDeAcesso("token-assinado", expiracao));
        AutenticacaoApiController api = new AutenticacaoApiController(usuarios, senhas, tokens);

        /* act */
        ResponseEntity<LoginResponse> resposta = api.autenticar(new LoginRequest("maria.silva", SENHA));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody())
                .isEqualTo(new LoginResponse(7L, "Maria Silva", "CLIENTE", "token-assinado", expiracao));
    }
}
