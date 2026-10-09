package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeItemCardapioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.feijoada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Atualizar e excluir item do cardápio")
class AlterarItemCardapioUseCaseTest {

    @Mock
    private IItemCardapioGateway itens;

    @Mock
    private IRestauranteGateway restaurantes;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        when(restaurantes.existeAtivo(9L)).thenReturn(true);
        when(restaurantes.existeAtivo(10L)).thenReturn(true);
        when(itens.buscarPorIdParaAlterar(5L)).thenReturn(Optional.of(feijoada()));
        when(itens.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("ITE-15 · CON-03 · atualizar sem mudar o nome é aceito: a verificação ignora o próprio item")
    void deveAtualizarMantendoONome() {
        /* act */
        ItemCardapio atualizado = AtualizarItemCardapioUseCase.create(itens, restaurantes, transacao)
                .run(atualizacao(9L, "Feijoada"));

        /* assert */
        verify(itens).existeNomeAtivoEmOutroItem(9L, "Feijoada", 5L);
        assertThat(atualizado.getPreco().valor()).hasToString("42.50");
        assertThat(atualizado.getApenasNoLocal()).isFalse();
        assertThat(atualizado.getCaminhoFoto()).isEqualTo("fotos/feijoada-grande.png");
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("ITE-16 · renomear para o nome de outro item ativo devolve conflito")
    void deveRecusarNomeDeOutroItem() {
        /* arrange */
        when(itens.existeNomeAtivoEmOutroItem(9L, "Moqueca", 5L)).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> AtualizarItemCardapioUseCase.create(itens, restaurantes, transacao)
                .run(atualizacao(9L, "Moqueca")))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O restaurante 9 já tem um item ativo chamado Moqueca.");
        verify(itens, never()).atualizar(any());
    }

    @Test
    @DisplayName("ITE-17 · atualizar pela rota de outro restaurante devolve não encontrado")
    void deveRecusarAtualizacaoPorOutroRestaurante() {
        /* act + assert */
        assertThatThrownBy(() -> AtualizarItemCardapioUseCase.create(itens, restaurantes, transacao)
                .run(atualizacao(10L, "Feijoada")))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Item 5 não encontrado no restaurante 10.");
        verify(itens, never()).atualizar(any());
    }

    @Test
    @DisplayName("ITE-18 · CON-03 · excluir marca o item como removido, dentro de uma transação")
    void deveExcluirLogicamente() {
        /* act */
        ExcluirItemCardapioUseCase.create(itens, restaurantes, transacao).run(9L, 5L);

        /* assert */
        verify(itens).remover(5L);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("ITE-17 · excluir pela rota de outro restaurante devolve não encontrado")
    void deveRecusarExclusaoPorOutroRestaurante() {
        /* act + assert */
        assertThatThrownBy(() -> ExcluirItemCardapioUseCase.create(itens, restaurantes, transacao).run(10L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(itens, never()).remover(anyLong());
    }

    private static AtualizacaoDeItemCardapioDTO atualizacao(Long restauranteId, String nome) {
        return new AtualizacaoDeItemCardapioDTO(restauranteId, 5L, nome, "Feijoada para duas pessoas.",
                new BigDecimal("42.5"), false, "fotos/feijoada-grande.png");
    }
}
