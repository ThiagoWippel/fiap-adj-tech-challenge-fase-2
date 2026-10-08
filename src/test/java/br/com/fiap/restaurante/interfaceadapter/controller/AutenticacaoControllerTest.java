package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.CredenciaisDTO;
import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.LoginResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Controller de autenticação")
class AutenticacaoControllerTest {

    @Mock
    private IUsuarioDataSource usuarios;

    @Mock
    private IPasswordEncoder senhas;

    @Mock
    private ITokenGenerator tokens;

    private AutoCloseable mocks;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("LOG-01 · autenticar devolve a resposta do login com o token")
    void deveAutenticar() {
        /* arrange */
        LocalDateTime expiracao = LocalDateTime.of(2026, 10, 8, 11, 0, 0);
        when(usuarios.buscarPorLogin("maria.silva")).thenReturn(Optional.of(DadosDeExemplo.maria()));
        when(senhas.confere(SENHA, SENHA_CODIFICADA)).thenReturn(true);
        when(tokens.gerar(any())).thenReturn(new TokenDeAcesso("token-assinado", expiracao));

        /* act */
        LoginResponse resposta = AutenticacaoController.create(usuarios, senhas, tokens)
                .autenticar(new CredenciaisDTO("maria.silva", SENHA));

        /* assert */
        assertThat(resposta).isEqualTo(new LoginResponse(7L, "Maria Silva", "CLIENTE", "token-assinado", expiracao));
    }
}
