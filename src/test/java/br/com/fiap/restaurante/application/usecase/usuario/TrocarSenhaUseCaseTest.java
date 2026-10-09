package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.TrocaDeSenhaDTO;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Trocar senha")
class TrocarSenhaUseCaseTest {

    private static final String NOVA_SENHA = "SenhaNova456";
    private static final String NOVA_SENHA_CODIFICADA = "$2a$10$hashDaSenhaNova456ParaTestesSemValorReal0000000000000000";

    @Mock
    private IUsuarioGateway usuarios;

    @Mock
    private IPasswordEncoder senhas;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private TrocarSenhaUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = TrocarSenhaUseCase.create(usuarios, senhas, transacao);
        when(usuarios.buscarPorIdParaAlterar(7L)).thenReturn(Optional.of(maria()));
        when(senhas.confere(SENHA, SENHA_CODIFICADA)).thenReturn(true);
        when(senhas.codificar(NOVA_SENHA)).thenReturn(NOVA_SENHA_CODIFICADA);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-15 · CON-03 · com a senha atual correta, grava a nova senha codificada")
    void deveGravarANovaSenhaCodificada() {
        /* act */
        useCase.run(new TrocaDeSenhaDTO(7L, SENHA, NOVA_SENHA));

        /* assert */
        ArgumentCaptor<Usuario> gravado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).atualizar(gravado.capture());
        assertThat(gravado.getValue().getSenha()).isEqualTo(NOVA_SENHA_CODIFICADA);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("USU-16 · com a senha atual incorreta, devolve credenciais inválidas e nada é gravado")
    void deveRecusarSenhaAtualIncorreta() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeSenhaDTO(7L, "SenhaErrada1", NOVA_SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("A senha atual informada está incorreta.");
        verify(senhas, never()).codificar(anyString());
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("USU-26 · nova senha fora de 8 a 72 caracteres é recusada")
    void deveRecusarNovaSenhaForaDoLimite() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeSenhaDTO(7L, SENHA, "curta")))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A senha deve ter entre 8 e 72 caracteres.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("USU-26 · usuário inexistente ou removido devolve não encontrado")
    void deveRecusarUsuarioInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeSenhaDTO(99L, SENHA, NOVA_SENHA)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
    }
}
