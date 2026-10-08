package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.NovoRestauranteDTO;
import br.com.fiap.restaurante.application.dto.TurnoDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Turno;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.ana;
import static br.com.fiap.restaurante.suporte.Exemplos.enderecoDTO;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static br.com.fiap.restaurante.suporte.Exemplos.turnosDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cadastrar restaurante")
class CadastrarRestauranteUseCaseTest {

    @Mock
    private IRestauranteGateway restaurantes;

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private CadastrarRestauranteUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = CadastrarRestauranteUseCase.create(restaurantes, usuarios, transacao);
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(ana()));
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(maria()));
        when(restaurantes.incluir(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-05 · cadastro com dono do tipo Dono de Restaurante é gravado, com os turnos convertidos e ordenados")
    void deveCadastrar() {
        /* act */
        Restaurante cadastrado = useCase.run(novo("ITALIANA", turnosDTO(), 8L));

        /* assert */
        ArgumentCaptor<Restaurante> gravado = ArgumentCaptor.forClass(Restaurante.class);
        verify(restaurantes).incluir(gravado.capture());
        assertThat(gravado.getValue().getDono().getId()).isEqualTo(8L);
        assertThat(gravado.getValue().getTipoCozinha()).isEqualTo(TipoCozinha.ITALIANA);
        assertThat(gravado.getValue().getHorarios().turnos()).extracting(Turno::toString)
                .containsExactly("SEGUNDA 11:00–15:00", "SEXTA 18:00–02:00");
        assertThat(cadastrado.getEndereco().cep()).isEqualTo("88301000");
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("RES-06 · dono inexistente ou removido devolve não encontrado")
    void deveRecusarDonoInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo("ITALIANA", turnosDTO(), 99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        verify(restaurantes, never()).incluir(any());
    }

    @Test
    @DisplayName("RES-07 · dono do tipo Cliente devolve conflito, orientando a trocar o tipo antes")
    void deveRecusarDonoCliente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo("ITALIANA", turnosDTO(), 7L)))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O usuário 7 não é Dono de Restaurante. Troque o tipo dele antes de cadastrar o restaurante.");
        verify(restaurantes, never()).incluir(any());
    }

    @Test
    @DisplayName("COZ-01 · tipo de cozinha fora da lista é recusado")
    void deveRecusarTipoDeCozinhaInvalido() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo("TAILANDESA", turnosDTO(), 8L)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessageStartingWith("O tipo de cozinha TAILANDESA não existe.");
        verify(restaurantes, never()).incluir(any());
    }

    @Test
    @DisplayName("HOR-05 · turnos sobrepostos são recusados e nada é gravado")
    void deveRecusarTurnosSobrepostos() {
        /* arrange */
        List<TurnoDTO> sobrepostos = List.of(new TurnoDTO("SEGUNDA", "11:00", "15:00"),
                new TurnoDTO("SEGUNDA", "14:00", "18:00"));

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo("ITALIANA", sobrepostos, 8L)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Os turnos SEGUNDA 11:00–15:00 e SEGUNDA 14:00–18:00 se sobrepõem.");
        verify(restaurantes, never()).incluir(any());
    }

    @Test
    @DisplayName("HOR-13 · horário em formato inválido é recusado")
    void deveRecusarHorarioInvalido() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo("ITALIANA", List.of(new TurnoDTO("SEGUNDA", "25:00", "15:00")), 8L)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O horário 25:00 não é válido. Use o formato HH:mm, como 18:30.");
        assertThatThrownBy(() -> useCase.run(novo("ITALIANA", List.of(new TurnoDTO("SEGUNDA", "11:00", null)), 8L)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O turno precisa de dia, abertura e fechamento.");
    }

    @Test
    @DisplayName("HOR-10 · sem turnos, o cadastro é recusado")
    void deveRecusarSemTurnos() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo("ITALIANA", null, 8L)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("Informe ao menos um turno de funcionamento.");
    }

    @Test
    @DisplayName("RES-03 · sem endereço, o cadastro é recusado")
    void deveRecusarSemEndereco() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new NovoRestauranteDTO("Cantina", null, "ITALIANA", turnosDTO(), 8L)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O endereço é obrigatório.");
        verify(restaurantes, never()).incluir(any());
    }

    private static NovoRestauranteDTO novo(String tipoCozinha, List<TurnoDTO> turnos, Long donoId) {
        return new NovoRestauranteDTO("Cantina da Nona", enderecoDTO(), tipoCozinha, turnos, donoId);
    }
}
