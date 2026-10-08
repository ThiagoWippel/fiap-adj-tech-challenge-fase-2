package br.com.fiap.restaurante.infrastructure.api.comum;

import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Converte o {@link Pageable} do Spring no pedido de página dos casos de uso. O
 * tamanho máximo (50) vem de spring.data.web.pageable.max-page-size.
 */
public final class Paginacao {

    private Paginacao() {
    }

    /**
     * Sem a lista de campos, um sort inválido só falharia na consulta ao banco, como
     * erro 500, e daria para ordenar por colunas que a API não mostra, como a senha.
     */
    public static PedidoDePagina pedido(Pageable paginacao, Set<String> camposOrdenaveis) {
        List<PedidoDePagina.Ordem> ordenacao = paginacao.getSort().stream()
                .map(ordem -> ordem(ordem, camposOrdenaveis))
                .toList();
        return new PedidoDePagina(paginacao.getPageNumber(), paginacao.getPageSize(), ordenacao);
    }

    private static PedidoDePagina.Ordem ordem(Sort.Order ordem, Set<String> camposOrdenaveis) {
        if (!camposOrdenaveis.contains(ordem.getProperty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível ordenar por "
                    + ordem.getProperty() + ". Campos aceitos: " + String.join(", ", new TreeSet<>(camposOrdenaveis)) + ".");
        }
        return new PedidoDePagina.Ordem(ordem.getProperty(), ordem.isAscending());
    }
}
