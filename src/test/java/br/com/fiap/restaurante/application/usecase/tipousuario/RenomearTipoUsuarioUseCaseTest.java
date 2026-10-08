package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.dto.RenomeacaoDeTipoUsuarioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.entregador;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Renomear tipo de usuário")
class RenomearTipoUsuarioUseCaseTest {

    @Mock
    private ITipoUsuarioGateway tipos;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private RenomearTipoUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = RenomearTipoUsuarioUseCase.create(tipos, transacao);
        when(tipos.buscarPorId(1L)).thenReturn(Optional.of(cliente()));
        when(tipos.buscarPorId(3L)).thenReturn(Optional.of(entregador()));
        when(tipos.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-04 · renomear grava o novo nome e mantém o código")
    void deveRenomearMantendoOCodigo() {
        /* act */
        TipoUsuario renomeado = useCase.run(new RenomeacaoDeTipoUsuarioDTO(3L, "Entregador Parceiro"));

        /* assert */
        assertThat(renomeado.getNome()).isEqualTo("Entregador Parceiro");
        assertThat(renomeado.getCodigo()).isEqualTo("ENTREGADOR");
        verify(tipos).atualizar(renomeado);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("TIP-09 · renomear um tipo de sistema é permitido e o código continua o mesmo")
    void deveRenomearTipoDeSistema() {
        /* act */
        TipoUsuario renomeado = useCase.run(new RenomeacaoDeTipoUsuarioDTO(1L, "Cliente Final"));

        /* assert */
        assertThat(renomeado.getNome()).isEqualTo("Cliente Final");
        assertThat(renomeado.getCodigo()).isEqualTo("CLIENTE");
        assertThat(renomeado.ehDeSistema()).isTrue();
    }

    @Test
    @DisplayName("TIP-08 · renomear para o nome de outro tipo devolve conflito")
    void deveRecusarNomeDeOutroTipo() {
        /* arrange */
        when(tipos.existeNomeEmOutroTipo("Cliente", 3L)).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new RenomeacaoDeTipoUsuarioDTO(3L, "Cliente")))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("Já existe um tipo de usuário com o nome Cliente.");
        verify(tipos, never()).atualizar(any());
    }

    @Test
    @DisplayName("TIP-08 · renomear tipo inexistente devolve não encontrado")
    void deveRecusarTipoInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new RenomeacaoDeTipoUsuarioDTO(99L, "Qualquer Nome")))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Tipo de usuário 99 não encontrado.");
        verify(tipos, never()).atualizar(any());
    }
}
