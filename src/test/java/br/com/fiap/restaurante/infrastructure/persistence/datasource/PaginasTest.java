package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina.Ordem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Conversão da paginação para o Spring Data")
class PaginasTest {

    @Test
    @DisplayName("PAG-08 · o id entra como desempate depois dos campos pedidos")
    void deveDesempatarPeloId() {
        /* act */
        PageRequest pagina = Paginas.paraPageRequest(new PedidoDePagina(1, 7, List.of(new Ordem("nome", false))));

        /* assert */
        assertThat(pagina.getPageNumber()).isEqualTo(1);
        assertThat(pagina.getPageSize()).isEqualTo(7);
        assertThat(pagina.getSort()).containsExactly(Sort.Order.desc("nome"), Sort.Order.asc("id"));
    }

    @Test
    @DisplayName("PAG-08 · ordenação que já usa o id não ganha um segundo desempate")
    void naoDeveRepetirOId() {
        /* act */
        PageRequest pagina = Paginas.paraPageRequest(new PedidoDePagina(0, 10, List.of(new Ordem("id", false))));

        /* assert */
        assertThat(pagina.getSort()).containsExactly(Sort.Order.desc("id"));
    }

    @Test
    @DisplayName("PAG-01 · a página do Spring Data vira a página dos casos de uso, com cada registro convertido")
    void deveConverterAPagina() {
        /* arrange */
        PageRequest pedido = Paginas.paraPageRequest(new PedidoDePagina(1, 2, List.of(new Ordem("preco", true))));

        /* act */
        Pagina<String> pagina = Paginas.paraPagina(new PageImpl<>(List.of("feijoada", "moqueca"), pedido, 5),
                String::toUpperCase);

        /* assert */
        assertThat(pedido.getSort()).containsExactly(Sort.Order.asc("preco"), Sort.Order.asc("id"));
        assertThat(pagina).isEqualTo(new Pagina<>(List.of("FEIJOADA", "MOQUECA"), 1, 2, 5, 3));
    }
}
