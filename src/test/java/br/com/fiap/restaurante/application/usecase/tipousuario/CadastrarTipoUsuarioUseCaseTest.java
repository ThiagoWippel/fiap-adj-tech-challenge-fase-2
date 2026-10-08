package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cadastrar tipo de usuário")
class CadastrarTipoUsuarioUseCaseTest {

    @Mock
    private ITipoUsuarioGateway tipos;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private CadastrarTipoUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = CadastrarTipoUsuarioUseCase.create(tipos, transacao);
        when(tipos.buscarPorCodigo(any())).thenReturn(Optional.empty());
        when(tipos.incluir(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-01 · cadastra o tipo com o código gerado do nome, dentro de uma transação")
    void deveCadastrarComOCodigoGerado() {
        /* act */
        TipoUsuario cadastrado = useCase.run("  Ajudante de Cozinha ");

        /* assert */
        ArgumentCaptor<TipoUsuario> gravado = ArgumentCaptor.forClass(TipoUsuario.class);
        verify(tipos).incluir(gravado.capture());
        assertThat(gravado.getValue().getNome()).isEqualTo("Ajudante de Cozinha");
        assertThat(gravado.getValue().getCodigo()).isEqualTo("AJUDANTE_DE_COZINHA");
        assertThat(cadastrado.ehDeSistema()).isFalse();
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("TIP-06 · nome já cadastrado devolve conflito e nada é gravado")
    void deveRecusarNomeJaCadastrado() {
        /* arrange */
        when(tipos.existeNome("Gerência")).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run("Gerência"))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("Já existe um tipo de usuário com o nome Gerência.");
        verify(tipos, never()).incluir(any());
    }

    @Test
    @DisplayName("TIP-07 · nome cujo código gerado já pertence a outro tipo devolve conflito")
    void deveRecusarCodigoJaUsado() {
        /* arrange */
        when(tipos.buscarPorCodigo("AJUDANTE_DE_COZINHA"))
                .thenReturn(Optional.of(TipoUsuario.create(4L, "Ajudante de Cozinha", "AJUDANTE_DE_COZINHA")));

        /* act + assert */
        assertThatThrownBy(() -> useCase.run("Ajudante-de-Cozinha"))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O nome Ajudante-de-Cozinha gera o código AJUDANTE_DE_COZINHA, que já pertence ao tipo "
                        + "Ajudante de Cozinha.");
        verify(tipos, never()).incluir(any());
    }

    @Test
    @DisplayName("TIP-03 · nome fora de 3 a 50 caracteres é recusado antes de consultar o banco")
    void deveRecusarNomeInvalido() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run("Ab"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O nome do tipo deve ter entre 3 e 50 caracteres.");
        verify(tipos, never()).existeNome(any());
        verify(tipos, never()).incluir(any());
    }
}
