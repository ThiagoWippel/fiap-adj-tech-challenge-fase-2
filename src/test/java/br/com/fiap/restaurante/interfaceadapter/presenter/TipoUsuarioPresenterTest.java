package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.entregador;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Presenter de tipo de usuário")
class TipoUsuarioPresenterTest {

    @Test
    @DisplayName("TIP-16 · a resposta traz id, nome, código e se o tipo é de sistema")
    void deveMontarAResposta() {
        /* act + assert */
        assertThat(TipoUsuarioPresenter.paraResposta(cliente()))
                .isEqualTo(new TipoUsuarioResponse(1L, "Cliente", "CLIENTE", true));
        assertThat(TipoUsuarioPresenter.paraResposta(entregador()))
                .isEqualTo(new TipoUsuarioResponse(3L, "Entregador", "ENTREGADOR", false));
    }

    @Test
    @DisplayName("TIP-16 · a página é convertida item a item, com os metadados")
    void deveConverterAPagina() {
        /* act */
        PaginaResponse<TipoUsuarioResponse> pagina =
                TipoUsuarioPresenter.paraPagina(new Pagina<>(List.of(cliente(), entregador()), 0, 10, 2, 1));

        /* assert */
        assertThat(pagina.conteudo()).extracting(TipoUsuarioResponse::codigo).containsExactly("CLIENTE", "ENTREGADOR");
        assertThat(pagina.totalElementos()).isEqualTo(2);
        assertThat(pagina.ultima()).isTrue();
    }
}
