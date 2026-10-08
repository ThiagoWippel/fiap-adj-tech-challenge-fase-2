package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeRestauranteDTO;
import br.com.fiap.restaurante.application.dto.TurnoDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Turno;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.cantina;
import static br.com.fiap.restaurante.suporte.Exemplos.enderecoDTO;
import static br.com.fiap.restaurante.suporte.Exemplos.joao;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Atualizar restaurante")
class AtualizarRestauranteUseCaseTest {

    private static final List<TurnoDTO> NOVOS_TURNOS = List.of(new TurnoDTO("TERCA", "18:00", "23:00"));

    @Mock
    private IRestauranteGateway restaurantes;

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private AtualizarRestauranteUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = AtualizarRestauranteUseCase.create(restaurantes, usuarios, transacao);
        when(restaurantes.buscarPorId(9L)).thenReturn(Optional.of(cantina()));
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(maria()));
        when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(joao()));
        when(restaurantes.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-08 · a atualização altera os dados e substitui todos os turnos, mantendo o dono")
    void deveAtualizarESubstituirOsTurnos() {
        /* act */
        Restaurante atualizado = useCase.run(atualizacao(8L));

        /* assert */
        assertThat(atualizado.getNome()).isEqualTo("Pizzaria da Nona");
        assertThat(atualizado.getTipoCozinha()).isEqualTo(TipoCozinha.PIZZARIA);
        assertThat(atualizado.getHorarios().turnos()).extracting(Turno::toString).containsExactly("TERCA 18:00–23:00");
        assertThat(atualizado.getDono().getId()).isEqualTo(8L);
        verify(usuarios, never()).buscarPorId(anyLong());
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("RES-09 · transferir para outro Dono de Restaurante é aceito")
    void deveTransferirParaOutroDono() {
        /* act */
        Restaurante atualizado = useCase.run(atualizacao(10L));

        /* assert */
        assertThat(atualizado.getDono().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("RES-09 · transferir para um Cliente devolve conflito; para usuário inexistente, não encontrado")
    void deveRecusarTransferenciaInvalida() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(atualizacao(7L)))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O usuário 7 não é Dono de Restaurante. Troque o tipo dele antes de transferir o restaurante.");
        assertThatThrownBy(() -> useCase.run(atualizacao(99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        verify(restaurantes, never()).atualizar(any());
    }

    @Test
    @DisplayName("RES-10 · atualizar restaurante inexistente ou removido devolve não encontrado")
    void deveRecusarRestauranteInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new AtualizacaoDeRestauranteDTO(99L, "Pizzaria da Nona", enderecoDTO(),
                "PIZZARIA", NOVOS_TURNOS, 8L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Restaurante 99 não encontrado.");
        verify(restaurantes, never()).atualizar(any());
    }

    @Test
    @DisplayName("RES-04 · sem endereço, a atualização é recusada e nada é gravado")
    void deveRecusarSemEndereco() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new AtualizacaoDeRestauranteDTO(9L, "Cantina", null, "ITALIANA",
                NOVOS_TURNOS, 8L)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O endereço é obrigatório.");
        verify(restaurantes, never()).atualizar(any());
    }

    private static AtualizacaoDeRestauranteDTO atualizacao(Long donoId) {
        return new AtualizacaoDeRestauranteDTO(9L, "Pizzaria da Nona", enderecoDTO(), "PIZZARIA", NOVOS_TURNOS, donoId);
    }
}
