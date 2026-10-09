package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.donoDeRestaurante;
import static br.com.fiap.restaurante.suporte.Exemplos.entregador;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Excluir tipo de usuário")
class ExcluirTipoUsuarioUseCaseTest {

    @Mock
    private ITipoUsuarioGateway tipos;

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private ExcluirTipoUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = ExcluirTipoUsuarioUseCase.create(tipos, usuarios, transacao);
        when(tipos.buscarPorIdParaAlterar(1L)).thenReturn(Optional.of(cliente()));
        when(tipos.buscarPorIdParaAlterar(2L)).thenReturn(Optional.of(donoDeRestaurante()));
        when(tipos.buscarPorIdParaAlterar(3L)).thenReturn(Optional.of(entregador()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-10 · CON-03 · tipo sem usuários ativos é excluído")
    void deveExcluirTipoSemUsuarios() {
        /* act */
        useCase.run(3L);

        /* assert */
        verify(tipos).excluir(3L);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @ParameterizedTest(name = "TIP-11 · tipo usado por {0} usuário(s) ativo(s) devolve conflito com a contagem")
    @CsvSource(delimiter = '|', value = {
            "1 | O tipo Entregador não pode ser excluído: 1 usuário ativo o usa.",
            "4 | O tipo Entregador não pode ser excluído: 4 usuários ativos o usam."
    })
    void deveRecusarTipoEmUso(long quantidade, String mensagem) {
        /* arrange */
        when(usuarios.contarAtivosPorTipo(3L)).thenReturn(quantidade);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(3L))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage(mensagem);
        verify(tipos, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("TIP-12 · Cliente e Dono de Restaurante são tipos de sistema e não podem ser excluídos")
    void deveRecusarTipoDeSistema() {
        /* arrange */
        TipoUsuario renomeado = TipoUsuario.create(1L, "Cliente Final", TipoUsuario.CODIGO_CLIENTE);
        when(tipos.buscarPorIdParaAlterar(1L)).thenReturn(Optional.of(renomeado));

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(1L))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O tipo Cliente Final é um tipo de sistema e não pode ser excluído.");
        assertThatThrownBy(() -> useCase.run(2L))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O tipo Dono de Restaurante é um tipo de sistema e não pode ser excluído.");
        verify(usuarios, never()).contarAtivosPorTipo(anyLong());
        verify(tipos, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("TIP-14 · excluir tipo inexistente devolve não encontrado")
    void deveRecusarTipoInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Tipo de usuário 99 não encontrado.");
        verify(tipos, never()).excluir(anyLong());
    }
}
