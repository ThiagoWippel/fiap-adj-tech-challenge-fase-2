package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.TrocaDeTipoDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
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

import static br.com.fiap.restaurante.suporte.Exemplos.CNPJ;
import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.ana;
import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.donoDeRestaurante;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Trocar o tipo do usuário")
class TrocarTipoDoUsuarioUseCaseTest {

    @Mock
    private IUsuarioGateway usuarios;

    @Mock
    private ITipoUsuarioGateway tipos;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private TrocarTipoDoUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = TrocarTipoDoUsuarioUseCase.create(usuarios, tipos, transacao);
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(maria()));
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(ana()));
        when(tipos.buscarPorCodigo(TipoUsuario.CODIGO_CLIENTE)).thenReturn(Optional.of(cliente()));
        when(tipos.buscarPorCodigo(TipoUsuario.CODIGO_DONO_RESTAURANTE)).thenReturn(Optional.of(donoDeRestaurante()));
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TRO-01 · cliente vira Dono de Restaurante com CNPJ, na mesma conta e numa transação")
    void deveTrocarClienteParaDono() {
        /* act */
        Usuario trocado = useCase.run(new TrocaDeTipoDTO(7L, "DONO_RESTAURANTE", "11.222.333/0001-81"));

        /* assert */
        ArgumentCaptor<Usuario> gravado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).atualizar(gravado.capture());
        assertThat(gravado.getValue().getId()).isEqualTo(7L);
        assertThat(gravado.getValue().ehDonoDeRestaurante()).isTrue();
        assertThat(gravado.getValue().getDocumento().numero()).isEqualTo(CNPJ);
        assertThat(trocado.getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("TRO-06 · dono sem restaurante ativo vira Cliente informando CPF")
    void deveTrocarDonoParaCliente() {
        /* act */
        Usuario trocado = useCase.run(new TrocaDeTipoDTO(8L, "CLIENTE", "52998224725"));

        /* assert */
        assertThat(trocado.getTipo().getCodigo()).isEqualTo("CLIENTE");
        assertThat(trocado.getDocumento().numero()).isEqualTo("52998224725");
    }

    @Test
    @DisplayName("TRO-07 · trocar para o tipo atual com o mesmo documento é aceito, sem gravar nada")
    void deveAceitarTrocaSemMudanca() {
        /* act */
        Usuario resultado = useCase.run(new TrocaDeTipoDTO(7L, "CLIENTE", "123.456.789-09"));

        /* assert */
        assertThat(resultado.getTipo().getCodigo()).isEqualTo("CLIENTE");
        assertThat(resultado.getDocumento().numero()).isEqualTo(CPF);
        verify(usuarios, never()).atualizar(any());
        verify(usuarios, never()).existeDocumentoEmOutroUsuario(anyString(), anyLong());
    }

    @Test
    @DisplayName("TRO-04 · documento já usado por outro usuário devolve conflito e nada é gravado")
    void deveRecusarDocumentoDeOutroUsuario() {
        /* arrange */
        when(usuarios.existeDocumentoEmOutroUsuario(CNPJ, 7L)).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeTipoDTO(7L, "DONO_RESTAURANTE", CNPJ)))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O documento informado já está cadastrado.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("TRO-02 · trocar para Dono de Restaurante com CPF é recusado e nada é gravado")
    void deveRecusarCpfParaDono() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeTipoDTO(7L, "DONO_RESTAURANTE", "52998224725")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Usuário do tipo Dono de Restaurante deve informar CNPJ.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("TRO-08 · documento ausente ou com tamanho de nenhum dos dois é recusado")
    void deveRecusarDocumentoAusenteOuInvalido() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeTipoDTO(7L, "DONO_RESTAURANTE", null)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O CNPJ é obrigatório para usuários do tipo Dono de Restaurante.");
        assertThatThrownBy(() -> useCase.run(new TrocaDeTipoDTO(7L, "DONO_RESTAURANTE", "123456")))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O documento deve ser um CPF ou um CNPJ.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("TRO-05 · usuário inexistente ou removido, ou tipo inexistente, devolve não encontrado")
    void deveRecusarUsuarioOuTipoInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new TrocaDeTipoDTO(99L, "CLIENTE", CPF)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        assertThatThrownBy(() -> useCase.run(new TrocaDeTipoDTO(7L, "ENTREGADOR", CPF)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Tipo de usuário ENTREGADOR não encontrado.");
        verify(usuarios, never()).atualizar(any());
    }
}
