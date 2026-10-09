package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.ana;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Excluir usuário")
class ExcluirUsuarioUseCaseTest {

    @Mock
    private IUsuarioGateway usuarios;

    @Mock
    private IRestauranteGateway restaurantes;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private ExcluirUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = ExcluirUsuarioUseCase.create(usuarios, restaurantes, transacao);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("EXC-01 · excluir usuário anonimiza o registro")
    void deveAnonimizarOUsuario() {
        /* arrange */
        when(usuarios.buscarPorIdParaAlterar(7L)).thenReturn(Optional.of(maria()));

        /* act */
        useCase.run(7L);

        /* assert */
        verify(usuarios).anonimizar(7L);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("CON-01 · o usuário é reservado antes da contagem de restaurantes e só depois anonimizado")
    void deveReservarOUsuarioAntesDeConferir() {
        /* arrange */
        when(usuarios.buscarPorIdParaAlterar(7L)).thenReturn(Optional.of(maria()));

        /* act */
        useCase.run(7L);

        /* assert */
        InOrder ordem = inOrder(usuarios, restaurantes);
        ordem.verify(usuarios).buscarPorIdParaAlterar(7L);
        ordem.verify(restaurantes).contarAtivosPorDono(7L);
        ordem.verify(usuarios).anonimizar(7L);
        verify(usuarios, never()).buscarPorId(anyLong());
    }

    @Test
    @DisplayName("EXC-02 · excluir usuário inexistente ou já removido devolve não encontrado")
    void deveRecusarUsuarioInexistente() {
        /* arrange */
        when(usuarios.buscarPorIdParaAlterar(99L)).thenReturn(Optional.empty());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        verify(usuarios, never()).anonimizar(anyLong());
    }

    @ParameterizedTest(name = "EXC-07 · usuário responsável por {0} restaurante(s) ativo(s) não é excluído")
    @CsvSource(delimiter = '|', value = {
            "1 | O usuário 8 é responsável por 1 restaurante ativo. Transfira ou exclua o restaurante antes de excluir o usuário.",
            "3 | O usuário 8 é responsável por 3 restaurantes ativos. Transfira ou exclua os restaurantes antes de excluir o usuário."
    })
    void deveRecusarUsuarioComRestauranteAtivo(long quantidade, String mensagem) {
        /* arrange */
        when(usuarios.buscarPorIdParaAlterar(8L)).thenReturn(Optional.of(ana()));
        when(restaurantes.contarAtivosPorDono(8L)).thenReturn(quantidade);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(8L))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage(mensagem);
        verify(usuarios, never()).anonimizar(anyLong());
    }
}
