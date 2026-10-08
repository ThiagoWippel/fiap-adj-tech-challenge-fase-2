package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.fiap.restaurante.suporte.Exemplos.cantina;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Presenter de restaurante")
class RestaurantePresenterTest {

    @Test
    @DisplayName("RES-12 · a resposta traz o dono como id e nome e os turnos ordenados, em HH:mm")
    void deveMontarAResposta() {
        /* act */
        RestauranteResponse resposta = RestaurantePresenter.paraResposta(cantina());

        /* assert */
        assertThat(resposta.id()).isEqualTo(9L);
        assertThat(resposta.nome()).isEqualTo("Cantina da Nona");
        assertThat(resposta.tipoCozinha()).isEqualTo("ITALIANA");
        assertThat(resposta.dono()).isEqualTo(new DonoResponse(8L, "Ana Souza"));
        assertThat(resposta.horarios()).containsExactly(new TurnoResponse("SEGUNDA", "11:00", "15:00"),
                new TurnoResponse("SEXTA", "18:00", "02:00"));
        assertThat(resposta.endereco().cidade()).isEqualTo("Itajaí");
        assertThat(resposta.dataCriacao()).isEqualTo(LocalDateTime.of(2026, 10, 1, 10, 0, 0));
    }

    @Test
    @DisplayName("RES-13 · a página é convertida item a item, com os metadados")
    void deveConverterAPagina() {
        /* act */
        PaginaResponse<RestauranteResponse> pagina =
                RestaurantePresenter.paraPagina(new Pagina<>(List.of(cantina()), 0, 10, 1, 1));

        /* assert */
        assertThat(pagina.conteudo()).extracting(RestauranteResponse::id).containsExactly(9L);
        assertThat(pagina.ultima()).isTrue();
    }
}
