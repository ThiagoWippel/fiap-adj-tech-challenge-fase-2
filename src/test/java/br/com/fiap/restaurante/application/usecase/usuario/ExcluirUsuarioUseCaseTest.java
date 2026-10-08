package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Excluir usuário")
class ExcluirUsuarioUseCaseTest {

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private ExcluirUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = ExcluirUsuarioUseCase.create(usuarios, transacao);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("EXC-01 · excluir usuário anonimiza o registro")
    void deveAnonimizarOUsuario() {
        /* arrange */
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(maria()));

        /* act */
        useCase.run(7L);

        /* assert */
        verify(usuarios).anonimizar(7L);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("EXC-02 · excluir usuário inexistente ou já removido devolve não encontrado")
    void deveRecusarUsuarioInexistente() {
        /* arrange */
        when(usuarios.buscarPorId(99L)).thenReturn(Optional.empty());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        verify(usuarios, never()).anonimizar(anyLong());
    }
}
