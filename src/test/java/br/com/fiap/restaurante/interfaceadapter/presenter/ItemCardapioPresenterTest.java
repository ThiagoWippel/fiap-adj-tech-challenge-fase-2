package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.fiap.restaurante.suporte.Exemplos.feijoada;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Presenter de item do cardápio")
class ItemCardapioPresenterTest {

    @Test
    @DisplayName("ITE-07 · a resposta traz o item com o preço em duas casas e o restaurante dono")
    void deveMontarAResposta() {
        /* act */
        ItemCardapioResponse resposta = ItemCardapioPresenter.paraResposta(feijoada());

        /* assert */
        assertThat(resposta.id()).isEqualTo(5L);
        assertThat(resposta.restauranteId()).isEqualTo(9L);
        assertThat(resposta.preco()).hasToString("39.90");
        assertThat(resposta.apenasNoLocal()).isTrue();
        assertThat(resposta.caminhoFoto()).isEqualTo("fotos/feijoada.jpg");
    }

    @Test
    @DisplayName("ITE-21 · a página é convertida item a item, com os metadados")
    void deveConverterAPagina() {
        /* act */
        PaginaResponse<ItemCardapioResponse> pagina =
                ItemCardapioPresenter.paraPagina(new Pagina<>(List.of(feijoada()), 0, 10, 1, 1));

        /* assert */
        assertThat(pagina.conteudo()).extracting(ItemCardapioResponse::nome).containsExactly("Feijoada");
        assertThat(pagina.totalElementos()).isEqualTo(1);
    }
}
