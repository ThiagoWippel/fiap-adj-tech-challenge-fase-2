package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.cantina;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Excluir restaurante")
class ExcluirRestauranteUseCaseTest {

    @Mock
    private IRestauranteGateway restaurantes;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private ExcluirRestauranteUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = ExcluirRestauranteUseCase.create(restaurantes, transacao);
        when(restaurantes.buscarPorId(9L)).thenReturn(Optional.of(cantina()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-11 · excluir marca o restaurante como removido, dentro de uma transação")
    void deveRemoverLogicamente() {
        /* act */
        useCase.run(9L);

        /* assert */
        verify(restaurantes).remover(9L);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("RES-10 · excluir restaurante inexistente ou já removido devolve não encontrado")
    void deveRecusarRestauranteInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Restaurante 99 não encontrado.");
        verify(restaurantes, never()).remover(anyLong());
    }
}
