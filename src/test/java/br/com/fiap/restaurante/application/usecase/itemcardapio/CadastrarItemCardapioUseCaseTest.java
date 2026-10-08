package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.dto.NovoItemCardapioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cadastrar item do cardápio")
class CadastrarItemCardapioUseCaseTest {

    @Mock
    private IItemCardapioGateway itens;

    @Mock
    private IRestauranteGateway restaurantes;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private CadastrarItemCardapioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = CadastrarItemCardapioUseCase.create(itens, restaurantes, transacao);
        when(restaurantes.existeAtivo(9L)).thenReturn(true);
        when(restaurantes.existeAtivo(10L)).thenReturn(true);
        when(itens.incluir(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("ITE-10 · ITE-03 · cadastro em restaurante ativo grava o item, com o nome sem espaços sobrando")
    void deveCadastrar() {
        /* act */
        ItemCardapio cadastrado = useCase.run(novo(9L, "  Feijoada   completa ", "39.9"));

        /* assert */
        ArgumentCaptor<ItemCardapio> gravado = ArgumentCaptor.forClass(ItemCardapio.class);
        verify(itens).incluir(gravado.capture());
        verify(itens).existeNomeAtivo(9L, "Feijoada completa");
        assertThat(gravado.getValue().getNome()).isEqualTo("Feijoada completa");
        assertThat(gravado.getValue().getPreco().valor()).hasToString("39.90");
        assertThat(cadastrado.getRestauranteId()).isEqualTo(9L);
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("ITE-11 · restaurante inexistente ou removido devolve não encontrado")
    void deveRecusarRestauranteInexistente() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo(99L, "Feijoada", "39.90")))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Restaurante 99 não encontrado.");
        verify(itens, never()).incluir(any());
    }

    @Test
    @DisplayName("ITE-12 · nome de outro item ativo do mesmo restaurante devolve conflito")
    void deveRecusarNomeRepetido() {
        /* arrange */
        when(itens.existeNomeAtivo(9L, "Feijoada")).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo(9L, "Feijoada", "39.90")))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O restaurante 9 já tem um item ativo chamado Feijoada.");
        verify(itens, never()).incluir(any());
    }

    @Test
    @DisplayName("ITE-14 · o mesmo nome em outro restaurante é aceito: a verificação é por restaurante")
    void deveAceitarMesmoNomeEmOutroRestaurante() {
        /* arrange */
        when(itens.existeNomeAtivo(9L, "Feijoada")).thenReturn(true);

        /* act */
        ItemCardapio cadastrado = useCase.run(novo(10L, "Feijoada", "42.00"));

        /* assert */
        assertThat(cadastrado.getRestauranteId()).isEqualTo(10L);
        verify(itens).existeNomeAtivo(10L, "Feijoada");
    }

    @Test
    @DisplayName("ITE-05 · preço com mais de duas casas é recusado antes de consultar o banco")
    void deveRecusarPrecoInvalido() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novo(9L, "Feijoada", "39.999")))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O preço deve ter no máximo duas casas decimais.");
        verify(itens, never()).existeNomeAtivo(anyLong(), anyString());
    }

    private static NovoItemCardapioDTO novo(Long restauranteId, String nome, String preco) {
        return new NovoItemCardapioDTO(restauranteId, nome, "Feijoada completa com farofa e couve.",
                new BigDecimal(preco), true, "fotos/feijoada.jpg");
    }
}
