package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Item do cardápio")
class ItemCardapioTest {

    private static final Preco PRECO = new Preco(new BigDecimal("39.90"));
    private static final String FOTO = "fotos/feijoada.jpg";

    @Test
    @DisplayName("ITE-01 · item válido é criado com nome, descrição, preço, disponibilidade e foto")
    void deveCriarItemValido() {
        /* act */
        ItemCardapio item = ItemCardapio.create(9L, "Feijoada", "Feijoada completa com farofa e couve.", PRECO,
                true, FOTO);

        /* assert */
        assertThat(item.getId()).isNull();
        assertThat(item.getRestauranteId()).isEqualTo(9L);
        assertThat(item.getNome()).isEqualTo("Feijoada");
        assertThat(item.getDescricao()).isEqualTo("Feijoada completa com farofa e couve.");
        assertThat(item.getPreco()).isEqualTo(PRECO);
        assertThat(item.getApenasNoLocal()).isTrue();
        assertThat(item.getCaminhoFoto()).isEqualTo(FOTO);
        assertThat(item.pertenceA(9L)).isTrue();
        assertThat(item.pertenceA(10L)).isFalse();
    }

    @Test
    @DisplayName("ITE-01 · ao ler do banco, o item vem com id e datas")
    void deveReconstituirComIdEDatas() {
        /* arrange */
        LocalDateTime criacao = LocalDateTime.of(2026, 10, 1, 10, 0);

        /* act */
        ItemCardapio item = ItemCardapio.create(5L, 9L, "Feijoada", "Feijoada completa.", PRECO, false, FOTO,
                criacao, criacao.plusHours(1));

        /* assert */
        assertThat(item.getId()).isEqualTo(5L);
        assertThat(item.getDataCriacao()).isEqualTo(criacao);
        assertThat(item.getDataUltimaAlteracao()).isEqualTo(criacao.plusHours(1));
        assertThat(item).isEqualTo(ItemCardapio.create(5L, 9L, "Outro", "Outra.", PRECO, true, FOTO, null, null));
    }

    @Test
    @DisplayName("ITE-03 · espaços sobrando no nome são removidos")
    void deveTirarEspacosSobrando() {
        /* act */
        ItemCardapio item = ItemCardapio.create(9L, "  Feijoada   completa ", "Feijoada.", PRECO, true, FOTO);

        /* assert */
        assertThat(item.getNome()).isEqualTo("Feijoada completa");
    }

    @ParameterizedTest(name = "ITE-02 · nome \"{0}\" é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"A", "   ", " B "})
    void deveRecusarNomeInvalido(String nome) {
        /* act + assert */
        assertThatThrownBy(() -> ItemCardapio.create(9L, nome, "Descrição.", PRECO, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O nome do item deve ter entre 2 e 100 caracteres.");
    }

    @Test
    @DisplayName("ITE-02 · nome com mais de 100 caracteres é recusado")
    void deveRecusarNomeLongoDemais() {
        /* act + assert */
        assertThatThrownBy(() -> ItemCardapio.create(9L, "a".repeat(101), "Descrição.", PRECO, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class);
    }

    @Test
    @DisplayName("ITE-02 · descrição vazia ou acima de 500 caracteres é recusada")
    void deveRecusarDescricaoInvalida() {
        /* act + assert */
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "  ", PRECO, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A descrição do item é obrigatória.");
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", null, PRECO, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class);
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "a".repeat(501), PRECO, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A descrição do item deve ter no máximo 500 caracteres.");
    }

    @Test
    @DisplayName("ITE-01 · preço, disponibilidade e restaurante são obrigatórios")
    void deveExigirPrecoDisponibilidadeERestaurante() {
        /* act + assert */
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "Feijoada.", null, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O preço é obrigatório.");
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, null, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("Informe se o item está disponível só para consumo no local.");
        assertThatThrownBy(() -> ItemCardapio.create(null, "Feijoada", "Feijoada.", PRECO, true, FOTO))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O restaurante do item é obrigatório.");
    }

    @ParameterizedTest(name = "ITE-08 · caminho de foto \"{0}\" é aceito")
    @ValueSource(strings = {"fotos/feijoada.jpg", "/img/pratos/FEIJOADA.JPEG", "https://cdn.exemplo.com/feijoada.png",
            "feijoada.webp"})
    void deveAceitarCaminhosDeFoto(String caminho) {
        /* act + assert */
        assertThat(ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, true, caminho).getCaminhoFoto())
                .isEqualTo(caminho);
    }

    @ParameterizedTest(name = "ITE-08 · caminho de foto \"{0}\" é recusado")
    @ValueSource(strings = {"fotos/feijoada.gif", "fotos/feijoada", "feijoada.jpg.txt"})
    void deveRecusarFotoSemExtensaoDeImagem(String caminho) {
        /* act + assert */
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, true, caminho))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O caminho da foto deve terminar em .jpg, .jpeg, .png ou .webp.");
    }

    @Test
    @DisplayName("ITE-08 · caminho de foto ausente ou acima de 255 caracteres é recusado")
    void deveRecusarFotoAusenteOuLongaDemais() {
        /* act + assert */
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, true, " "))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O caminho da foto é obrigatório.");
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, true, null))
                .isInstanceOf(ValidacaoDeDominioException.class);
        assertThatThrownBy(() -> ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, true,
                "fotos/" + "a".repeat(246) + ".jpg"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O caminho da foto deve ter no máximo 255 caracteres.");
    }

    @Test
    @DisplayName("ITE-02 · alterar um campo para um valor inválido falha e o item mantém o valor anterior")
    void deveManterOValorAnterior_QuandoAAlteracaoFalhar() {
        /* arrange */
        ItemCardapio item = ItemCardapio.create(9L, "Feijoada", "Feijoada.", PRECO, true, FOTO);

        /* act + assert */
        assertThatThrownBy(() -> item.setNome("X")).isInstanceOf(ValidacaoDeDominioException.class);
        assertThatThrownBy(() -> item.setCaminhoFoto("foto.gif")).isInstanceOf(ValidacaoDeDominioException.class);
        assertThat(item.getNome()).isEqualTo("Feijoada");
        assertThat(item.getCaminhoFoto()).isEqualTo(FOTO);
    }
}
