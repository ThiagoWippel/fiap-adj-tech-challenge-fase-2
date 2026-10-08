package br.com.fiap.restaurante.infrastructure.api.comum;

import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina.Ordem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Paginação")
class PaginacaoTest {

    private static final Set<String> CAMPOS = Set.of("nome", "email");

    @Test
    @DisplayName("PAG-01 · converte página, tamanho e ordenação do Spring para o pedido de página")
    void deveConverterOPageable() {
        /* act */
        PedidoDePagina pedido = Paginacao.pedido(
                PageRequest.of(2, 20, Sort.by(Sort.Order.desc("nome"), Sort.Order.asc("email"))), CAMPOS);

        /* assert */
        assertThat(pedido).isEqualTo(new PedidoDePagina(2, 20, List.of(new Ordem("nome", false), new Ordem("email", true))));
    }

    @Test
    @DisplayName("PAG-03 · ordenar por um campo fora da lista devolve 400, informando os campos aceitos")
    void deveRecusarCampoDeOrdenacaoDesconhecido() {
        /* act + assert */
        assertThatThrownBy(() -> Paginacao.pedido(PageRequest.of(0, 10, Sort.by("senha")), CAMPOS))
                .isInstanceOfSatisfying(ResponseStatusException.class, erro -> {
                    assertThat(erro.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(erro.getReason()).isEqualTo("Não é possível ordenar por senha. Campos aceitos: email, nome.");
                });
    }
}
